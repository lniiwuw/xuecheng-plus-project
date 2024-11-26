package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/29 10:13
 * @Description 修改课程信息的提交类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "EditCourseDto", description = "修改课程基本信息")
public class EditCourseDto extends AddCourseDto {
    @ApiModelProperty(value = "课程id", required = true)
    private long id;
}
