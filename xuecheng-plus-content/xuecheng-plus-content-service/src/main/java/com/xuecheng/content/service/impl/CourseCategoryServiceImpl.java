package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.ICourseCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/26 19:37
 * @Description 课程类别查询类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseCategoryServiceImpl implements ICourseCategoryService {

    private final CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        // 获取到所有节点，按课程类别等级从低到高排列
        List<CourseCategoryTreeDto> categoryTree = courseCategoryMapper.selectTreeNodes(id);
        // 存储id节点下的子节点
        HashMap<String, CourseCategoryTreeDto> mp = new HashMap<>();
        // 最终处理结果result
        List<CourseCategoryTreeDto> result = new ArrayList<>();
        categoryTree.forEach(item -> {
            // 找到id下属的子节点，并加入到result和mp
            if (item.getParentid().equals(id)) {
                mp.put(item.getId(), item);
                result.add(item);
            }

            // 添加id子节点的子节点
            String parentId = item.getParentid();
            CourseCategoryTreeDto parentNode = mp.get(parentId);
            if (parentNode != null) {
                // 初始化child数组
                List<CourseCategoryTreeDto> child = parentNode.getChildrenTreeNodes();
                if (child == null) {
                    parentNode.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                parentNode.getChildrenTreeNodes().add(item);
            }
        });
        return result;
    }
}
