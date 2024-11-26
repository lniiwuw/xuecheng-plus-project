package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseTeacher;
import lombok.Data;

import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/12 19:21
 * @Description freemarker渲染前端界面的课程信息模型类
 */
@Data
public class CoursePreviewDto {

    /**
     * 课程基本计划、课程营销信息
     */
    CourseBaseInfoDto courseBase;

    /**
     * 课程计划信息
     */
    List<TeachplanDto> teachplans;

    /**
     * 师资信息
     */
    CourseTeacher courseTeacher;
}
