package com.xuecheng.content.feignclient.fallback;

import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.feignclient.po.CourseIndex;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/15 16:15
 * @Description 调用SearchServiceClient的降级逻辑
 */
@Component
public class SearchServiceClientFallback implements FallbackFactory<SearchServiceClient> {
    @Override
    public SearchServiceClient create(Throwable cause) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                return null;
            }
        };
    }
}
