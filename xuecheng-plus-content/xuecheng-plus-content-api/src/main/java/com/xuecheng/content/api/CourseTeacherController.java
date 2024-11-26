package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.impl.CourseTeacherServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/1 21:10
 * @Description 课程-教师信息管理接口
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(value = "教师信息相关接口", tags = "教师信息相关接口")
public class CourseTeacherController {

    private final CourseTeacherServiceImpl courseTeacherService;

    @ApiOperation("查询教师信息接口")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> getCourseTeacherList(@PathVariable Long courseId) {
        return courseTeacherService.getCourseTeacherList(courseId);
    }

    @ApiOperation("添加/修改教师信息接口")
    @PostMapping("/courseTeacher")
    public CourseTeacher saveCourseTeacher(@RequestBody CourseTeacher courseTeacher) {
        long companyId = 1232141425L;
        return courseTeacherService.saveCourseTeacher(companyId, courseTeacher);
    }

    @ApiOperation("删除教师信息接口")
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long teacherId) {
        long companyId = 1232141425L;
        courseTeacherService.deleteCourseTeacher(companyId, courseId, teacherId);
    }
}
