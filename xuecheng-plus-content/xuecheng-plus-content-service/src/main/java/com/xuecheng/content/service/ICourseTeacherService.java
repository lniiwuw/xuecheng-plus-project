package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/1 21:11
 * @Description 教师信息管理接口类
 */
public interface ICourseTeacherService {
    /**
     * 根据课程id查询课程教师
     * @param courseId
     * @return CourseTeacher
     */
    public List<CourseTeacher> getCourseTeacherList(Long courseId);

    /**
     * 保存课程教师信息
     * @param courseTeacher
     * @return CourseTeacher
     */
    public CourseTeacher saveCourseTeacher(Long companyId, CourseTeacher courseTeacher);

    /**
     * 删除课程id为courseId下指定teacherId的教师
     * @param courseId
     * @param teacherId
     */
    public void deleteCourseTeacher(Long companyId, Long courseId, Long teacherId);
}
