package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.ICoursePublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/12 20:46
 * @Description 课程预览控制类
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/open")
public class CourseOpenController {

    private final ICoursePublishService coursePublishService;


    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable Long courseId) {
        // 获取课程预览信息
        return coursePublishService.getCoursePreviewInfo(courseId);
    }
}
