package com.xuecheng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/24 17:13
 * @Description 内容管理服务启动类
 */
@EnableFeignClients(basePackages = {"com.xuecheng.content.feignclient"})
@SpringBootApplication
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }
}
