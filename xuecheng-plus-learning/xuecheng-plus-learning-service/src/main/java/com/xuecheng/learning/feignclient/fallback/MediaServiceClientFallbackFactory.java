package com.xuecheng.learning.feignclient.fallback;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/27 9:05
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            @Override
            public RestResponse<String> getPlayUrlByMediaId(String mediaId) {
                log.error("远程调用媒资管理服务熔断异常：{}", throwable.getMessage());
                return null;
            }
        };
    }
}
