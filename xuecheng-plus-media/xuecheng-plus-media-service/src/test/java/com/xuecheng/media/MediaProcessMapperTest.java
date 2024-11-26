package com.xuecheng.media;

import com.xuecheng.media.config.MybatisPlusConfig;
import com.xuecheng.media.mapper.MediaProcessMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/8 21:09
 * @Description TODO
 */
public class MediaProcessMapperTest {

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Test
    void test() {
        int x = mediaProcessMapper.startTask(1L);
        System.out.println(x);
    }

}
