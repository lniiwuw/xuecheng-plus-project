package com.xuecheng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/25 16:14
 * @Description 测试启动类，与测试类在同一包下或者SpringbootTest注解指定启动类
 */
@EnableFeignClients(basePackages = {"com.xuecheng.content.feignclient"})
@SpringBootApplication
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }
}
