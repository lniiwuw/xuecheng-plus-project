package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/26 21:29
 * @Description 课程添加成功后的返回信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CourseBaseInfoDto extends CourseBase {

    /**
     * 收费规则，对应数据字典
     */
    private String charge;

    /**
     * 价格
     */
    private Float price;

    /**
     * 原价
     */
    private Float originalPrice;

    /**
     * 咨询qq
     */
    private String qq;

    /**
     * 微信
     */
    private String wechat;

    /**
     * 电话
     */
    private String phone;

    /**
     * 有效期天数
     */
    private Integer validDays;

    /**
     * 大分类名称
     */
    private String mtName;

    /**
     * 小分类名称
     */
    private String stName;

}
