package com.xuecheng.content.feignclient;

import com.xuecheng.content.feignclient.fallback.SearchServiceClientFallback;
import com.xuecheng.content.feignclient.po.CourseIndex;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/15 16:11
 * @Description search服务的远程调用接口
 */
@FeignClient(value = "search", fallbackFactory = SearchServiceClientFallback.class)
public interface SearchServiceClient {

    @PostMapping("/search/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);
}
