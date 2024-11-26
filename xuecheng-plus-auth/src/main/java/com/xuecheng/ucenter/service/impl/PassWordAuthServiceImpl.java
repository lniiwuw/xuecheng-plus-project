package com.xuecheng.ucenter.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/16 17:14
 * @Description 基于账号密码对比的认证实现类
 */
@RequiredArgsConstructor
@Service("password_authservice")
public class PassWordAuthServiceImpl implements IAuthService {

    private final XcUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        // 校验验证码
        String checkcode = authParamsDto.getCheckcode();
        String checkcodekey = authParamsDto.getCheckcodekey();

        if (StringUtils.isBlank(checkcodekey) || StringUtils.isBlank(checkcode)) {
            throw new RuntimeException("验证码为空");
        }
        if (!checkCodeClient.verify(checkcodekey, checkcode)) {
            throw new RuntimeException("验证码输入错误");
        }

        // 账号
        String username = authParamsDto.getUsername();
        // 查询用户
        XcUser user = userMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (user == null) {
            // 用户不存在
            throw new RuntimeException("账号不存在");
        }
        // 取出数据库存储的正确密码
        String password = user.getPassword(); // 加密后的正确密码
        String inputPwd = authParamsDto.getPassword(); // 输入的密码
        // 比对密码
        if (!passwordEncoder.matches(inputPwd, password)) {
            throw new RuntimeException("账号或密码错误");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);
        return xcUserExt;
    }
}
