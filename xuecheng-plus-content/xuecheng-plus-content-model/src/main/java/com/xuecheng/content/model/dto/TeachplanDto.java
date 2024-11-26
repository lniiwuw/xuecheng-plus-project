package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/30 20:46
 * @Description 课程计划信息 dto
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeachplanDto extends Teachplan {
    private TeachplanMedia teachplanMedia;
    private List<TeachplanDto> teachPlanTreeNodes;
}
