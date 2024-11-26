package com.xuecheng.content;

import com.xuecheng.content.service.ITeachplanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/1 16:43
 * @Description TODO
 */
@SpringBootTest
public class TeachplanServiceTest {

    @Autowired
    ITeachplanService teachplanService;

    @Test
    void testmoveTeachplan() {
        teachplanService.moveTeachplan("moveup", 269L);
    }
}
