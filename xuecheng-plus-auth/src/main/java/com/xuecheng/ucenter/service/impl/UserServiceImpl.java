package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/16 16:07
 * @Description UserDetailsService实现类
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserDetailsService {

    private final ApplicationContext applicationContext;
    private final XcUserMapper xcUserMapper;
    private final XcMenuMapper xcMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String json) throws UsernameNotFoundException {
        // 将传入的json转入请求参数对象
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(json, AuthParamsDto.class);
        } catch (Exception e) {
            throw new RuntimeException("请求认证参数不符合要求");
        }

        // 认证类型
        String authType = authParamsDto.getAuthType();
        // 拿到spring容器中的实例
        String beanName = authType + "_authservice";
        IAuthService authService = applicationContext.getBean(beanName, IAuthService.class);
        // 开始认证
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        return getUserDetails(xcUserExt);
    }

    /**
     * 根据 XcUserExt 对象构造一个 UserDetails 对象
     *
     * @param userExt userExt 对象-用户信息
     * @return UserDetails
     */
    public UserDetails getUserDetails(XcUserExt userExt) {
        // 查询用户权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userExt.getId());
        List<String> permissions = new ArrayList<>();
        if (xcMenus == null || xcMenus.size() == 0) {
            permissions.add("p1");
        } else {
            xcMenus.forEach(menu -> permissions.add(menu.getCode()));
        }
        // 将用户权限放在 XcUserExt 中
        userExt.setPermissions(permissions);

        String[] authorities = permissions.toArray(new String[0]);

        String password = userExt.getPassword();
        // 为了安全在令牌中不存放密码
        userExt.setPassword(null);

        // TODO: 设置默认机构id为1232141425
        userExt.setCompanyId(Long.valueOf(1232141425).toString());

        String userJson = JSON.toJSONString(userExt);
        return User.withUsername(userJson).password(password).authorities(authorities).build();
    }
}
