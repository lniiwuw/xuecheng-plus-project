package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/31 10:41
 * @Description TeachplanMapper测试
 */
@SpringBootTest
public class TeachplanMapperTest {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Test
    void testTeachplanMapper() {
        List<TeachplanDto> list = teachplanMapper.selectTreeNodes(117L);
        System.out.println(list);
    }

    @Test
    void testSelectOrderbyMax() {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, 117L);
        queryWrapper.eq(Teachplan::getParentid, 268L);
        int count = teachplanMapper.selectOrderbyMax(queryWrapper);
        System.out.println(count);
    }
}
