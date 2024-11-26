package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.ICourseTeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/1 21:12
 * @Description 教师信息实现类
 */
@Service
@RequiredArgsConstructor
public class CourseTeacherServiceImpl implements ICourseTeacherService {

    private final CourseTeacherMapper courseTeacherMapper;
    private final CourseBaseMapper courseBaseMapper;

    @Override
    public List<CourseTeacher> getCourseTeacherList(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        return courseTeacherMapper.selectList(queryWrapper);
    }

    @Transactional
    @Override
    public CourseTeacher saveCourseTeacher(Long companyId, CourseTeacher courseTeacher) {
        // 判断课程是否属于当前机构
        checkCourseBelongCompany(companyId, courseTeacher.getCourseId());
        Long id = courseTeacher.getId();
        // id为空表示新增操作，否则为修改
        if (id == null) {
            courseTeacher.setCreateDate(LocalDateTime.now());
            int insert = courseTeacherMapper.insert(courseTeacher);
            if (insert <= 0) {
                XueChengPlusException.cast("新增教师失败！");
            }
        } else {
            int update = courseTeacherMapper.updateById(courseTeacher);
            if (update <= 0) {
                XueChengPlusException.cast("修改教师信息失败！");
            }
        }
        return courseTeacherMapper.selectById(courseTeacher.getId());
    }

    @Transactional
    @Override
    public void deleteCourseTeacher(Long companyId, Long courseId, Long teacherId) {
        // 判断课程是否属于当前机构
        checkCourseBelongCompany(companyId, courseId);
        // 删除操作
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId)
                .eq(CourseTeacher::getId, teacherId);
        int delete = courseTeacherMapper.delete(queryWrapper);
        if (delete <= 0) {
            XueChengPlusException.cast("删除教师失败！");
        }
    }

    /**
     * 判断更新教师信息的课程是否属于当前机构
     * @param companyId
     * @param courseId
     */
    private void checkCourseBelongCompany(Long companyId, Long courseId) {
        // 查询课程信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            XueChengPlusException.cast("课程不存在！");
        }
        // 判断当前课程是否属于当前机构
        if (!courseBase.getCompanyId().equals(companyId)) {
            XueChengPlusException.cast("只允许修改本机构课程！");
        }
    }
}
