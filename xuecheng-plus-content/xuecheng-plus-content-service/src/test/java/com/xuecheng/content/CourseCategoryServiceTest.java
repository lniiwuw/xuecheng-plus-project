package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.ICourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/26 20:23
 * @Description 课程分类测试类
 */

@SpringBootTest
public class CourseCategoryServiceTest {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Autowired
    ICourseCategoryService courseCategoryService;

    @Test
    void testCourseCategoryMapper() {
        List<CourseCategoryTreeDto> list = courseCategoryMapper.selectTreeNodes("1");
        System.out.println(list);
    }

    @Test
    void testCourseCategoryService() {
        List<CourseCategoryTreeDto> categoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        System.out.println(categoryTreeDtos);
    }
}
