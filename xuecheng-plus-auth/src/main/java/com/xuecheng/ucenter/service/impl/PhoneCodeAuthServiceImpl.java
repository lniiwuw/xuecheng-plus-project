package com.xuecheng.ucenter.service.impl;

import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/16 18:33
 * @Description 手机验证码登录
 */
@RequiredArgsConstructor
@Service("phone_authservice")
public class PhoneCodeAuthServiceImpl implements IAuthService {

    private final CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        // 获取手机验证码
        String checkcode = authParamsDto.getCheckcode();
        // 获取验证码key
        String checkcodekey = authParamsDto.getCheckcodekey();

        if (!checkCodeClient.verify(checkcodekey, checkcode)) {
            throw new RuntimeException("验证码错误");
        }

        // 获取手机号
        String phone = authParamsDto.getCellphone();
        // TODO: 根据手机号生成用户信息并添加到数据库

        return null;
    }
}
