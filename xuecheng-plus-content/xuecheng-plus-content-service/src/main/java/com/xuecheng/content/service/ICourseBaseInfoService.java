package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/25 18:18
 * @Description 课程信息管理接口
 */
public interface ICourseBaseInfoService {
    /**
     * 课程分页查询
     * @param companyId 机构id
     * @param pageParams 分页参数信息
     * @param queryCourseParamDto 课程查询的参数模型类
     * @return PageResult<CourseBase>
     */
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamDto queryCourseParamDto);

    /**
     *  新增课程
     * @param companyId 机构id
     * @param addCourseDto 课程信息
     * @return CourseBaseInfoDto
     */
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /**
     * 根据课程id查询课程信息，包括基本信息和营销信息
     * @param courseId 课程id
     * @return CourseBaseInfoDto
     */
    public CourseBaseInfoDto queryCourseBaseById(Long courseId);

    /**
     * 根据课程id更新课程信息
     * @param companyId 机构id，只有本机构才能修改课程
     * @param editCourseDto 提交的课程更新信息
     * @return CourseBaseInfoDto
     */
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);

    /**
     * 删除整个课程
     * @param companyId 机构id
     * @param courseId 课程id
     */
    public void deleteCourse(Long companyId, Long courseId);
}
