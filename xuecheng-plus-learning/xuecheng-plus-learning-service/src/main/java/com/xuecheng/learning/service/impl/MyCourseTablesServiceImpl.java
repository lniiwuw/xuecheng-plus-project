package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.IMyCourseTablesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/18 20:36
 * @Description 选课接口实现类
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MyCourseTablesServiceImpl implements IMyCourseTablesService {

    private final XcChooseCourseMapper xcChooseCourseMapper;
    private final XcCourseTablesMapper xcCourseTablesMapper;
    private final ContentServiceClient serviceClient;

    @Transactional
    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        // 查询课程信息
        CoursePublish coursePublish = serviceClient.getCoursePublish(courseId);
        if (coursePublish == null) {
            XueChengPlusException.cast("课程信息不存在！");
        }
        // 收费规则
        String charge = coursePublish.getCharge();
        XcChooseCourse xcChooseCourse = null;
        if ("201000".equals(charge)) {
            // 1 免费课程
            log.debug("添加免费课程， courseId：{}", courseId);
            // 1.1 向选课记录表
            xcChooseCourse = addFreeCourse(userId, coursePublish);
            // 1.2 向我的课程表添加数据
            addCourseTables(xcChooseCourse);
        } else {
            // 2 如果是收费课程，向选课记录表添加数据
            log.debug("添加收费课程，courseId：{}", courseId);
            xcChooseCourse = addChargeCourse(userId, coursePublish);
        }

        // 判断学生的学习资格
        XcCourseTablesDto xcCourseTablesDto = getLearningStatus(userId, courseId);

        // 封装返回值
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse, xcChooseCourseDto);
        // 设置学习资格状态
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());
        return xcChooseCourseDto;
    }

    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        XcCourseTablesDto courseTablesDto = new XcCourseTablesDto();
        // 1. 查询我的课程表
        XcCourseTables courseTables = getXcCourseTables(userId, courseId);
        // 2. 未查到，返回一个状态码为"702002"的对象，没有选课或者课后没有支付
        if (courseTables == null) {
            courseTablesDto = new XcCourseTablesDto();
            courseTablesDto.setLearnStatus("702002");
            return courseTablesDto;
        }
        // 3. 查到了，判断是否过期
        boolean isExpires = LocalDateTime.now().isAfter(courseTables.getValidtimeEnd());
        BeanUtils.copyProperties(courseTables, courseTablesDto);
        if (isExpires) {
            // 3.1 已过期，返回状态码为"702003"的对象
            courseTablesDto.setLearnStatus("702003");
        } else {
            // 3.2 未过期，返回状态码为"702001"的对象
            courseTablesDto.setLearnStatus("702001");
        }
        return courseTablesDto;
    }

    @Override
    public boolean saveChooseCourseStatus(String chooseCourseId) {
        // 1.根据选课记录id查询选课信息
        XcChooseCourse xcChooseCourse = xcChooseCourseMapper.selectById(chooseCourseId);
        if (xcChooseCourse == null) {
            log.error("接收到购买课程的消息，根据选课id未查询到课程，选课id：{}", chooseCourseId);
            return false;
        }
        // 2. 选课状态为未支付时，更新选课状态为选课成功
        if ("701002".equals(xcChooseCourse.getStatus())) {
            xcChooseCourse.setStatus("701001");
            int update = xcChooseCourseMapper.updateById(xcChooseCourse);
            if (update <= 0) {
                log.error("更新选课记录失败：{}", xcChooseCourse);
            }
        }
        // 3. 向我的课程表添加记录
        addCourseTables(xcChooseCourse);
        log.error("更新选课记录成功：{}", xcChooseCourse);
        return true;
    }

    @Override
    public PageResult<XcCourseTables> myCourseTables(MyCourseTableParams params) {
        // 分页参数
        int pageNo = params.getPage();
        int pageSize = params.getSize();
        // 用户id
        String userId = params.getUserId();
        // 查询用户课程
        Page<XcCourseTables> page = new Page<>(pageNo, pageSize);
        Page<XcCourseTables> xcCourseTablesPage = xcCourseTablesMapper.selectPage(page, new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId));
        // 得到记录总数和数据
        long total = xcCourseTablesPage.getTotal();
        List<XcCourseTables> records = xcCourseTablesPage.getRecords();
        // 封装数据返回
        return new PageResult<XcCourseTables>(records, total, pageNo, pageSize);
    }

    /**
     * 添加到我的课程表
     *
     * @param chooseCourse 选课记录
     */
    private void addCourseTables(XcChooseCourse chooseCourse) {
        String status = chooseCourse.getStatus();
        if (!"701001".equals(status)) {
            XueChengPlusException.cast("选课未成功，无法添加到课程表");
        }
        XcCourseTables courseTables = getXcCourseTables(chooseCourse.getUserId(), chooseCourse.getCourseId());
        // 有则返回，无则插入
        if (courseTables != null) {
            return;
        }
        courseTables = new XcCourseTables();
        BeanUtils.copyProperties(chooseCourse, courseTables);
        courseTables.setChooseCourseId(chooseCourse.getId());
        courseTables.setCourseType(chooseCourse.getOrderType());
        courseTables.setUpdateDate(LocalDateTime.now());
        int insert = xcCourseTablesMapper.insert(courseTables);
        if (insert <= 0) {
            XueChengPlusException.cast("添加我的课程表失败");
        }
    }

    /**
     * 根据用户id和课程id查询我的课程表中的某一门课程
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return 我的课程表中的课程
     */
    public XcCourseTables getXcCourseTables(String userId, Long courseId) {
        return xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getUserId, userId)
                .eq(XcCourseTables::getCourseId, courseId));
    }

    /**
     * 将付费课程加入到选课记录表
     *
     * @param userId   用户id
     * @param coursePublish 课程信息
     * @return 选课记录
     */
    private XcChooseCourse addChargeCourse(String userId, CoursePublish coursePublish) {
        // 1. 先判断是否已经存在对应的选课，因为数据库中没有约束，所以可能存在相同数据的选课
        LambdaQueryWrapper<XcChooseCourse> lambdaQueryWrapper = new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursePublish.getId())
                .eq(XcChooseCourse::getOrderType, "700002")  // 收费课程
                .eq(XcChooseCourse::getStatus, "701002");// 待支付
        // 1.1 由于可能存在多条，所以这里用selectList
        List<XcChooseCourse> chooseCourses = xcChooseCourseMapper.selectList(lambdaQueryWrapper);
        // 1.2 如果已经存在对应的选课数据，返回一条即可
        if (!chooseCourses.isEmpty()) {
            return chooseCourses.get(0);
        }
        // 2. 数据库中不存在数据，添加选课信息，对照着数据库中的属性挨个set即可
        XcChooseCourse chooseCourse = new XcChooseCourse();
        chooseCourse.setCourseId(coursePublish.getId());
        chooseCourse.setCourseName(coursePublish.getName());
        chooseCourse.setUserId(userId);
        chooseCourse.setCompanyId(coursePublish.getCompanyId());
        chooseCourse.setOrderType("700002"); // 收费课程
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setCoursePrice(coursePublish.getPrice());
        chooseCourse.setValidDays(365);
        chooseCourse.setStatus("701002"); // 待支付
        chooseCourse.setValidtimeStart(LocalDateTime.now());
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int insert = xcChooseCourseMapper.insert(chooseCourse);
        if (insert <= 0) {
            XueChengPlusException.cast("添加选课记录失败");
        }
        return chooseCourse;
    }

    /**
     * 将免费课程加入到选课表
     *
     * @param userId   用户id
     * @param coursePublish 课程信息
     * @return 选课记录
     */
    private XcChooseCourse addFreeCourse(String userId, CoursePublish coursePublish) {
        // 1. 先判断是否已经存在对应的选课，因为数据库中没有约束，所以可能存在相同数据的选课
        LambdaQueryWrapper<XcChooseCourse> lambdaQueryWrapper = new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursePublish.getId())
                .eq(XcChooseCourse::getOrderType, "700001")  // 免费课程
                .eq(XcChooseCourse::getStatus, "701007");// 选课成功
        // 1.1 由于可能存在多条，所以这里用selectList
        List<XcChooseCourse> chooseCourses = xcChooseCourseMapper.selectList(lambdaQueryWrapper);
        // 1.2 如果已经存在对应的选课数据，返回一条即可
        if (!chooseCourses.isEmpty()) {
            return chooseCourses.get(0);
        }

        // 2. 数据库中不存在数据，添加选课信息，对照着数据库中的属性挨个set即可
        XcChooseCourse chooseCourse = new XcChooseCourse();
        chooseCourse.setCourseId(coursePublish.getId());
        chooseCourse.setCourseName(coursePublish.getName());
        chooseCourse.setUserId(userId);
        chooseCourse.setCompanyId(coursePublish.getCompanyId());
        chooseCourse.setOrderType("700001"); // 免费课程
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setCoursePrice(coursePublish.getPrice());
        chooseCourse.setValidDays(365);
        chooseCourse.setStatus("701001"); //选课成功
        chooseCourse.setValidtimeStart(LocalDateTime.now());
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int insert = xcChooseCourseMapper.insert(chooseCourse);
        if (insert <= 0) {
            XueChengPlusException.cast("添加选课记录失败！");
        }
        return chooseCourse;
    }
}
