package com.xuecheng.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/24 17:00
 * @Description 响应的结果模型类
 */
@Data
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    // 数据列表
    private List<T> items;
    // 总记录数
    private long counts;
    // 当前页码
    private long page;
    // 每页记录数
    private long pageSize;
}
