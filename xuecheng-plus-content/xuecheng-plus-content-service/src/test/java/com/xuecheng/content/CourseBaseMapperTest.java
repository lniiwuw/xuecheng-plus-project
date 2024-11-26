package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.CourseBase;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/25 16:09
 * @Description
 */
@SpringBootTest
public class CourseBaseMapperTest {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Test
    void testCourseMapper() {
        CourseBase courseBase = courseBaseMapper.selectById(1);
        Assertions.assertNotNull(courseBase);

        // 定义查询的dto类
        QueryCourseParamDto courseParamDto = new QueryCourseParamDto();
        courseParamDto.setCourseName("java");

        // mybatis-plus查询
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNoneEmpty(courseParamDto.getCourseName()), CourseBase::getName, courseParamDto.getCourseName())
                .eq(StringUtils.isNoneEmpty(courseParamDto.getAuditStatus()), CourseBase::getAuditStatus, courseParamDto.getAuditStatus())
                .eq(StringUtils.isNoneEmpty(courseParamDto.getPublishStatus()), CourseBase::getStatus, courseParamDto.getPublishStatus());

        // 分页参数
        Page<CourseBase> page = new Page<CourseBase>(1L, 2L);
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 拿到数据和条数
        List<CourseBase> courseList = pageResult.getRecords();
        long total = pageResult.getTotal();

    }
}
