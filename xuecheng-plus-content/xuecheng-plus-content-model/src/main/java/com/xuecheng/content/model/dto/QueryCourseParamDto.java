package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/24 16:54
 * @Description 课程查询的参数模型类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("查询的参数模型类")
public class QueryCourseParamDto {
    // 审核状态
    @ApiModelProperty("审核状态")
    private String auditStatus;
    // 课程名称
    @ApiModelProperty("课程名称")
    private String courseName;
    // 发布状态
    @ApiModelProperty("发布状态")
    private String publishStatus;

}
