package com.xuecheng.content.feignclient.fallback;

import com.xuecheng.content.feignclient.MediaServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/15 14:11
 * @Description 调用MediaServiceClient的降级逻辑
 */
@Slf4j
@Component
public class MediaServiceClientFallback implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable cause) {
        return new MediaServiceClient() {
            // 发生熔断时，调用上传服务执行该降级逻辑
            @Override
            public String upload(MultipartFile upload, String folder, String objectName) {
                log.debug("熔断处理，熔断异常：{}", cause.getMessage());
                return null;
            }
        };
    }
}
