package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/18 20:35
 * @Description 选课接口
 */
public interface IMyCourseTablesService {
    /**
     * 添加选课
     * @param userId    用户id
     * @param courseId  课程id
     */
    XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * 获取学习资格
     * @param userId        用户id
     * @param courseId      课程id
     * @return  学习资格状态
     */
    XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    /**
     * 更新相应的选课记录状态为成功
     *
     * @param chooseCourseId 课程id
     */
    boolean saveChooseCourseStatus(String chooseCourseId);

    /**
     * 获取我的课程表
     *
     * @param params 请求参数
     * @return PageResult<XcCourseTables>
     */
    PageResult<XcCourseTables> myCourseTables(MyCourseTableParams params);
}
