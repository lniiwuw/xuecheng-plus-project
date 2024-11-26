package com.xuecheng.media.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/3 19:32
 * @Description 从nacos配置文件中取minio相关配置信息
 */
@Configuration
public class MinioConfig {
    // 在 @Configuration 类被处理时，Spring 会首先解析 @Value 注解并注入值
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder().
                endpoint(endpoint).
                credentials(accessKey, secretKey).
                build();
    }
}
