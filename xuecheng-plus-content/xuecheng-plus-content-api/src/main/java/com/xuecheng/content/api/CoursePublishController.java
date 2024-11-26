package com.xuecheng.content.api;

import com.alibaba.fastjson.JSON;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.ICoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;


/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/12 19:05
 * @Description 课程发布相关控制类
 */
@Api(value = "课程发布接口",tags = "课程发布接口")
@Slf4j
@Controller
@RequiredArgsConstructor
public class CoursePublishController {

    private final ICoursePublishService coursePublishService;

    @ApiOperation("课程预览")
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){
        ModelAndView modelAndView = new ModelAndView();
        // 指定模版
        modelAndView.setViewName("course_template");
        // 指定模型
        modelAndView.addObject("model", coursePublishService.getCoursePreviewInfo(courseId));
        return modelAndView;
    }

    @ApiOperation("提交课程审核")
    @ResponseBody // 需要添加此注解，否则出现错误 Exiting from "ERROR" dispatch, status 404
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable Long courseId) {
        long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId, courseId);
    }

    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping ("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId){
        long companyId = 1232141425L;
        coursePublishService.publish(companyId, courseId);
    }

    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursePublish(@PathVariable("courseId") Long courseId) {
        return coursePublishService.getCoursePublish(courseId);
    }

    @ApiOperation("获取课程发布信息")
    @ResponseBody
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursePreviewDto(@PathVariable("courseId") Long courseId) {
        // 从数据库中查
        // return coursePublishService.getCoursePreviewInfo(courseId);

        // 从redis缓存中查
        CoursePublish coursePublish = coursePublishService.getCoursePublishCache(courseId);
        // 不存在课程
        if (coursePublish == null) {
            return new CoursePreviewDto();
        }
        //课程基本信息
        CourseBaseInfoDto courseBase = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursePublish, courseBase);
        //课程计划
        List<TeachplanDto> teachplan = JSON.parseArray(coursePublish.getTeachplan(), TeachplanDto.class);
        CoursePreviewDto coursePreviewInfo = new CoursePreviewDto();
        coursePreviewInfo.setCourseBase(courseBase);
        coursePreviewInfo.setTeachplans(teachplan);
        return coursePreviewInfo;
    }
}
