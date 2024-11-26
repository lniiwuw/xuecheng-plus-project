package com.xuecheng.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/15 13:11
 * @Description feign客户端测试
 */
@SpringBootTest
public class FeignClientTest {

    @Autowired
    MediaServiceClient mediaServiceClient;


    @Test
    void testMediaClient() {
        File file = new File("D:/Java_Project/xuecheng-plus-project/test.html");
        mediaServiceClient.upload(MultipartSupportConfig.getMultipartFile(file), "course", "test.html");
    }
}
