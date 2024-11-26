package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/16 17:10
 * @Description 同一认证接口
 */
public interface IAuthService {

    /**
     * 认证方法
     *
     * @param authParamsDto 认证请求参数
     * @return XcUserExt
     */
    XcUserExt execute(AuthParamsDto authParamsDto);
}
