package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/31 10:53
 * @Description 课程计划管理相关接口
 */
public interface ITeachplanService {
    /**
     * 根据课程id查询课程
     * @param courseId 课程id
     * @return List<TeachplanDto>
     */
    public List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * 新增或修改课程计划
     * @param saveTeachplanDto
     */
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     * 根据课程计划id删除课程计划，大章节要求无小章节后才可删除
     * @param teachplanId
     */
    public void deleteTeachplan(Long teachplanId);

    /**
     * 向上/向下移动课程计划
     * @param moveType
     * @param teachplanId
     */
    public void moveTeachplan(String moveType, Long teachplanId);

    /**
     * 教学计划绑定媒资信息
     * @param bindTeachplanMediaDto 传递的绑定信息
     */
    void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /**
     * 根据课程计划id查询课程计划信息
     *
     * @param teachPlanId 课程计划id
     * @return Teachplan
     */
    Teachplan getTeachPlan(Long teachPlanId);
}
