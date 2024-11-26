package com.xuecheng.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.system.mapper.DictionaryMapper;
import com.xuecheng.system.model.po.Dictionary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/26 15:42
 * @Description 数据库连接测试类
 */

@SpringBootTest
public class TestMapper {

    @Autowired
    DictionaryMapper dictionaryMapper;

    @Test
    void dictionaryMapperTest() {
        LambdaQueryWrapper<Dictionary> eq = new LambdaQueryWrapper<Dictionary>().eq(Dictionary::getCode, 000);
        List<Dictionary> dictionaries = dictionaryMapper.selectList(eq);
        System.out.println(dictionaries);
    }
}
