package com.xuecheng.checkcode.service.impl;

import com.xuecheng.checkcode.service.CheckCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Mr.M
 * @version 1.0
 * @description 使用redis存储验证码，测试用
 * @date 2022/9/29 18:36
 */
@RequiredArgsConstructor
@Component("RedisCheckCodeStore")
public class RedisCheckCodeStore implements CheckCodeService.CheckCodeStore {

    public final StringRedisTemplate stringRedisTemplate;

    @Override
    public void set(String key, String value, Integer expire) {
        stringRedisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
    }

    @Override
    public String get(String key) {
        return (String) stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void remove(String key) {
        stringRedisTemplate.delete(key);
    }
}
