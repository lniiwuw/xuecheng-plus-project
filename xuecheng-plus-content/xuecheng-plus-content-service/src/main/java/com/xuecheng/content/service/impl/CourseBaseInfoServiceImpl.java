package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.ICourseBaseInfoService;
import com.xuecheng.content.service.ICourseMarketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/25 18:20
 * @Description 课程信息管理实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseBaseInfoServiceImpl implements ICourseBaseInfoService {

    private final CourseBaseMapper courseBaseMapper;
    private final CourseMarketMapper courseMarketMapper;
    private final CourseCategoryMapper courseCategoryMapper;
    private final ICourseMarketService courseMarketService;
    private final CourseTeacherMapper courseTeacherMapper;
    private final TeachplanMapper teachplanMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamDto queryCourseParamDto) {
        // mybatis-plus查询
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNoneEmpty(queryCourseParamDto.getCourseName()), CourseBase::getName, queryCourseParamDto.getCourseName())
                .eq(StringUtils.isNoneEmpty(queryCourseParamDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamDto.getAuditStatus())
                .eq(StringUtils.isNoneEmpty(queryCourseParamDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamDto.getPublishStatus())
                .eq(CourseBase::getCompanyId, companyId);

        // 分页参数
        Page<CourseBase> page = new Page<CourseBase>(pageParams.getPageNo(), pageParams.getPageSize());
        // 得到查询结果
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 拿到数据和条数
        List<CourseBase> courseList = pageResult.getRecords();
        long total = pageResult.getTotal();

        // 封装结果
        return new PageResult<>(courseList, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Override
    @Transactional
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {
        // 对参数进行合法性校验 controller和一些其他服务调用
        // 合法性校验
        if (StringUtils.isBlank(addCourseDto.getName())) {
            XueChengPlusException.cast("课程名称为空");
        }
        if (StringUtils.isBlank(addCourseDto.getMt())) {
            XueChengPlusException.cast("课程分类为空");
        }
        if (StringUtils.isBlank(addCourseDto.getSt())) {
            XueChengPlusException.cast("课程分类为空");
        }
        if (StringUtils.isBlank(addCourseDto.getGrade())) {
            XueChengPlusException.cast("课程等级为空");
        }
        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
            XueChengPlusException.cast("教育模式为空");
        }
        if (StringUtils.isBlank(addCourseDto.getUsers())) {
            XueChengPlusException.cast("适应人群为空");
        }
        if (StringUtils.isBlank(addCourseDto.getCharge())) {
            XueChengPlusException.cast("收费规则为空");
        }

        // 向课程基本信息表course_base写入数据
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto, courseBase);
        // companyId赋值放在后面，防止拷贝后覆盖
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        // 审核状态默认为未提交
        courseBase.setAuditStatus("202002");
        // 分布状态默认为未发布
        courseBase.setStatus("203001");
        int insert = courseBaseMapper.insert(courseBase);
        // 添加失败
        if (insert <= 0) {
            throw new RuntimeException("添加课程失败");
        }

        // 向课程营销表course_market写入
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto, courseMarket);
        // 插入成功后，mybatis默认的主键回显  插入课程id
        courseMarket.setId(courseBase.getId());
        // 保存营销信息
        int insert2 = saveCourseMarket(courseMarket);
        if (insert2 <= 0) {
            XueChengPlusException.cast("添加课程失败");
        }

        return getCourseBaseInfo(courseBase.getId());
    }

    @Override
    public CourseBaseInfoDto queryCourseBaseById(Long courseId) {
        return getCourseBaseInfo(courseId);
    }

    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        long courseId = editCourseDto.getId();
        // 查询课程信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            XueChengPlusException.cast("课程不存在！");
        }
        // 判断当前课程是否属于当前机构
        if (!courseBase.getCompanyId().equals(companyId)) {
            XueChengPlusException.cast("只允许修改本机构课程！");
        }
        // 拷贝属性
        BeanUtils.copyProperties(editCourseDto, courseBase);
        // 设置更新时间
        courseBase.setChangeDate(LocalDateTime.now());
        //  修改
        int updateCnt = courseBaseMapper.updateById(courseBase);
        if (updateCnt <= 0) {
            XueChengPlusException.cast("修改课程失败！");
        }

        // 查询营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 课程营销信息不是必填项，故这里先判断一下
        if (courseMarket == null) {
            courseMarket = new CourseMarket();
        }
        // 拷贝属性
        BeanUtils.copyProperties(editCourseDto, courseMarket);
        // 有则更新，无则插入
        int update = this.saveCourseMarket(courseMarket);
        log.info("CourseMarket更新条数：{}", update);
        return getCourseBaseInfo(courseId);
    }

    @Transactional
    @Override
    public void deleteCourse(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!companyId.equals(courseBase.getCompanyId())) {
            XueChengPlusException.cast("只允许删除本机构的课程");
        }
        // 课程的审核状态必须为未提交，状态码 202002
        if (!"202002".equals(courseBase.getAuditStatus())) {
            XueChengPlusException.cast("提交后课程不能再删除！");
        }
        // 删除课程教师信息
        LambdaQueryWrapper<CourseTeacher> TeacherQueryWrapper = new LambdaQueryWrapper<>();
        TeacherQueryWrapper.eq(CourseTeacher::getCourseId, courseId);
        courseTeacherMapper.delete(TeacherQueryWrapper);
        // 删除课程计划
        LambdaQueryWrapper<Teachplan> teachplanQueryWrapper = new LambdaQueryWrapper<>();
        teachplanQueryWrapper.eq(Teachplan::getCourseId, courseId);
        teachplanMapper.delete(teachplanQueryWrapper);
        // 删除营销信息
        courseMarketMapper.deleteById(courseId);
        // 删除课程基本信息
        courseBaseMapper.deleteById(courseId);
    }

    /**
     * 根据课程id封装课程基本信息和营销信息
     * @param courseId
     * @return CourseBaseInfoDto
     */
    public CourseBaseInfoDto getCourseBaseInfo(long courseId) {
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        // 查询课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        // 拷贝属性
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);

        // 查询课程基本信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }
        // 查询分类名称
        String mt = courseBase.getMt();
        String st = courseBase.getSt();
        CourseCategory  mtCourseCategory = courseCategoryMapper.selectById(mt);
        CourseCategory stCourseCategory = courseCategoryMapper.selectById(st);
        if (mtCourseCategory != null) {
            // 填充大分类名称
            courseBaseInfoDto.setMtName(mtCourseCategory.getName());
        }
        if (stCourseCategory != null) {
            // 填充小分类名称
            courseBaseInfoDto.setStName(stCourseCategory.getName());
        }
        return courseBaseInfoDto;
    }

    /**
     *  保存营销信息的方法
     * @param courseMarket
     * @return int
     */
    public int saveCourseMarket(CourseMarket courseMarket) {
        // 参数合法校验
        String charge = courseMarket.getCharge();
        if (StringUtils.isEmpty(charge)) {
            XueChengPlusException.cast("收费规则为空");
        }

        if (charge.equals("201001")) {
            if (courseMarket.getPrice() == null || courseMarket.getPrice() <= 0) {
                XueChengPlusException.cast("课程的价格不能为空并且大于0");
            }
        }

        return courseMarketService.saveOrUpdate(courseMarket) ? 1 : 0;
    }
}
