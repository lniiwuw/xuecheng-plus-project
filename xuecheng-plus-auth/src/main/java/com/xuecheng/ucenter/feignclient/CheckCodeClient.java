package com.xuecheng.ucenter.feignclient;

import com.xuecheng.ucenter.feignclient.fallback.CheckCodeClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/16 20:40
 * @Description checkCode服务的远程调用客户端
 */
@FeignClient(value = "checkcode", fallbackFactory = CheckCodeClientFallback.class)
public interface CheckCodeClient {

    /**
     * 验证码校验
     *
     * @param key 键值
     * @param code 验证码
     * @return Boolean
     */
    // 注意：两个参数，至少得有一个使用RequestParam注解，否则报错
    @PostMapping("/checkcode/verify")
    public Boolean verify(@RequestParam("key")String key, @RequestParam("code")String code);
}
