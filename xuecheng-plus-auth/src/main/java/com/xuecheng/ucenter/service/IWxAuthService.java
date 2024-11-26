package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/17 16:48
 * @Description 微信扫码认证
 */
public interface IWxAuthService {
    /**
     * 微信扫码认证，申请令弹，携带令牌查询用户信息，保存用户信息到数据库
     *
     * @param code 授权码
     * @return XcUser
     */
    XcUser wxAuth(String code);
}
