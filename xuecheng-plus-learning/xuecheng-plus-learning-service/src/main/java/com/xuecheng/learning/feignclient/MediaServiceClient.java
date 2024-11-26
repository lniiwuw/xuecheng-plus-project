package com.xuecheng.learning.feignclient;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.learning.feignclient.fallback.MediaServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description 媒资服务远程接口
 * @author Mr.M
 * @date 2022/10/27 9:04
 * @version 1.0
 */
@FeignClient(value = "media-api", fallbackFactory = MediaServiceClientFallbackFactory.class)
public interface MediaServiceClient {

    @GetMapping("/media/open/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable("mediaId") String mediaId);
}
