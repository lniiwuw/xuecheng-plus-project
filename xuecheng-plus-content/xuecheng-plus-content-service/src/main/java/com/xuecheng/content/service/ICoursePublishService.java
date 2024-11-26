package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/12 19:23
 * @Description 课程预览信息接口类
 */
public interface ICoursePublishService {
    /**
     * 根据课程id获取课程预览信息
     * @param courseId  课程id
     * @return  package com.xuecheng.content.model.dto.CoursePreviewDto;
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     * @param companyId 机构id
     * @param courseId  课程id
     */
    void commitAudit(Long companyId, Long courseId);

    /**
     * 课程发布
     *
     * @param companyId 机构id
     * @param courseId 课程id
     */
    void publish(Long companyId, Long  courseId);

    /**
     * 生成课程静态化页面
     *
     * @param courseId 课程id
     * @return File
     */
    File generateCourseHtml(Long courseId);

    /**
     * 上传文件到minio
     *
     * @param courseId 课程id
     * @param file 上传的文件
     */
    void uploadCourseHtml(Long courseId, File file);

    /**
     * 根据id查询课程发布信息
     *
     * @param courseId 课程id
     * @return CoursePublish
     */
    CoursePublish getCoursePublish(Long courseId);

    /**
     * @description 查询缓存中的课程信息
     * @param courseId 课程id
     * @return com.xuecheng.content.model.po.CoursePublish
     * @author Mr.M
     * @date 2022/10/22 16:15
     */
    public CoursePublish getCoursePublishCache(Long courseId);
}
