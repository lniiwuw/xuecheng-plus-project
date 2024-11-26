package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author lniiwuw
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {
    // 查询课程计划
    public List<TeachplanDto> selectTreeNodes(Long courseId);

    // 查询teachplan表中符合wrapper条件的orderby的最大值
    @Select("select max(orderby) from teachplan ${ew.customSqlSegment}")
    public Integer selectOrderbyMax(@Param("ew") LambdaQueryWrapper<Teachplan> wrapper);
}
