package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.IAuthService;
import com.xuecheng.ucenter.service.impl.WxAuthServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/16 21:49
 * @Description 微信授权
 */
@Slf4j
@RequiredArgsConstructor
// 注意：需要重定向，此处应是controller
@Controller
public class WxLoginController {

    private final WxAuthServiceImpl wxAuthService;

    /**
     * 微信扫码回调
     *
     * @param code 微信授权码
     * @param state redis中验证码的键key
     * @return String
     */
    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调，code：{}，state：{}", code, state);
        XcUser user = wxAuthService.wxAuth(code);
        if (user == null) {
            return "redirect:http://www.51xuecheng.cn/error.html";
        }
        log.debug("重定向到授权界面");
        String username = user.getUsername();
        return "redirect:http://www.51xuecheng.cn/sign.html?username=" + username + "&authType=wx";
    }
}
