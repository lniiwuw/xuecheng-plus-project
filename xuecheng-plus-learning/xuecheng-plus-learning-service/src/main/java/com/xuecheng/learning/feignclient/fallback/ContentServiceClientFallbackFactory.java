package com.xuecheng.learning.feignclient.fallback;

import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/25 9:14
 */
@Slf4j
@Component
public class ContentServiceClientFallbackFactory implements FallbackFactory<ContentServiceClient> {
    @Override
    public ContentServiceClient create(Throwable throwable) {
        return new ContentServiceClient() {
            @Override
            public CoursePublish getCoursePublish(Long courseId) {
                log.error("调用内容管理服务发生熔断:{}", throwable.toString(),throwable);
                return null;
            }

            @Override
            public CourseBaseInfoDto getCourseById(Long courseId) {
                return null;
            }

            @Override
            public Teachplan getTeachplan(Long teachplanId) {
                log.error("调用内容管理服务发生熔断:{}", throwable.toString(),throwable);
                return null;
            }
        };
    }
}
