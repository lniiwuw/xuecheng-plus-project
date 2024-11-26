package com.xuecheng.base.model;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("分页参数类")
public class PageParams {
    // 默认起始页码
    public static final long DEFAULT_PAGE_CURRENT = 1L;
    // 默认每页记录数
    public static final long DEFAULT_PAGE_SIZE = 10L;

    // 当前页码
    @ApiModelProperty("页码")
    private Long pageNo = DEFAULT_PAGE_CURRENT;

    // 当前每页记录数
    @ApiModelProperty("每页记录数")
    private Long pageSize = DEFAULT_PAGE_SIZE;
}