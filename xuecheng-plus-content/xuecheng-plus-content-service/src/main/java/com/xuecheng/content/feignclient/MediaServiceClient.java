package com.xuecheng.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.fallback.MediaServiceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/14 21:54
 * @Description media-service的远程调用接口
 */
@FeignClient(value = "media-api",
        configuration = {MultipartSupportConfig.class},
        fallbackFactory = MediaServiceClientFallback.class)
public interface MediaServiceClient {

    @RequestMapping(value = "/media/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String upload(@RequestPart("filedata") MultipartFile upload,
                                      @RequestParam(value = "folder", required = false) String folder,
                                      @RequestParam(value = "objectName", required = false) String objectName);
}
