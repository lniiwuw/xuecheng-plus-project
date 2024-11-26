package com.xuecheng.ucenter.feignclient.fallback;

import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/16 20:45
 * @Description CheckCodeClient客户端的降级逻辑
 */
@Slf4j
@Component
public class CheckCodeClientFallback implements FallbackFactory<CheckCodeClient> {
    @Override
    public CheckCodeClient create(Throwable cause) {
        return new CheckCodeClient() {
            @Override
            public Boolean verify(String key, String code) {
                log.debug("执行checkcode#verify的降级逻辑， key：{}，code：{}", key, code);
                return false;
            }
        };
    }
}
