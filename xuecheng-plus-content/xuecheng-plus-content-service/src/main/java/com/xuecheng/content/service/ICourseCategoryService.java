package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/26 19:36
 * @Description 课程分类服务接口
 */
public interface ICourseCategoryService {
    /**
     * 查询从id开始的课程分类树形结果
     * @param id
     * @return List<CourseCategoryTreeDto>
     */
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
