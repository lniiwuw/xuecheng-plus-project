package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcRoleMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.IAuthService;
import com.xuecheng.ucenter.service.IWxAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/16 18:31
 * @Description 微信授权登录
 */
@Slf4j
@RequiredArgsConstructor
@Service("wx_authservice")
public class WxAuthServiceImpl implements IAuthService, IWxAuthService {

    private final XcUserMapper xcUserMapper;
    private final RestTemplate restTemplate;
    private final XcUserRoleMapper xcUserRoleMapper;

    @Lazy
    @Autowired
    WxAuthServiceImpl currentProxy;

    @Value("${weixin.appid}")
    private String appid;

    @Value("${weixin.secret}")
    private String secret;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        // 账号
        String username = authParamsDto.getUsername();
        // 查询用户
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (user == null) {
            // 用户不存在
            throw new RuntimeException("账号不存在");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);
        return xcUserExt;
    }


    @Override
    public XcUser wxAuth(String code) {
        // 申请令牌
        Map<String, String> accessTokenMap = getAccessToken(code);

        // 携带令牌查询用户信息
        String openid = accessTokenMap.get("openid");
        String accessToken = accessTokenMap.get("access_token");
        Map<String, String> userInfo = getUserInfo(accessToken, openid);

        // 查询用户信息，不存在则插入
        return currentProxy.addWxUser(userInfo);
    }

    /**
     * 根据授权码得到微信返回的令牌
     *
     * @param code 授权码
     * @return Map<String>
     */
    /*微信返回的令牌信息示例
    {
        *  "access_token":"ACCESS_TOKEN",
        *  "expires_in":7200,
        *  "refresh_token":"REFRESH_TOKEN",
        *  "openid":"OPENID",
        *  "scope":"SCOPE",
        *  "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
    }
     */
    private Map<String, String> getAccessToken(String code) {
        String wxUrl_template =
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        // 请求路径
        String wxUrl = String.format(wxUrl_template, appid, secret, code);
        log.info("调用微信接口申请令牌, url:{}", wxUrl);
        // 远程调用url
        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);
        // 获取响应结果
        String body = exchange.getBody();
        log.info("申请的令牌信息:{}", body);
        // 转为map并返回
        Map<String, String> map = JSON.parseObject(body, Map.class);
        return map;
    };

    /**
     * 根据令牌和openid向微信申请信息
     *
     * @param accessToken 令牌
     * @param openid openid
     * @return Map<String>
     */
    /*微信返回的信息示例
     * <pre>
     * {
     *    "openid": "OPENID",
     *    "nickname": "NICKNAME",
     *    "sex": 1,
     *    "province": "PROVINCE",
     *    "city": "CITY",
     *    "country": "COUNTRY",
     *    "headimgurl": "<a href="https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0">headimgurl</a>",
     *    "privilege": [
     *        "PRIVILEGE1",
     *        "PRIVILEGE2"
     *    ],
     *    "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     */
    private Map<String, String> getUserInfo(String accessToken, String openid) {
        String wxUrl_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        // 请求微信地址
        String wxUrl = String.format(wxUrl_template, accessToken, openid);
        log.info("调用微信接口申请用户信息, url:{}", wxUrl);
        // 响应信息
        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);
        String result = exchange.getBody();
        // 转码为UTF-8编码
        assert result != null;
        result = new String(result.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // 5. 转为map
        log.info("申请的用户信息:{}", result);
        return JSON.parseObject(result, Map.class);
    }

    /**
     * 查询用户信息，不存在则插入
     *
     * @param userInfo 微信返回的用户信息键值对
     * @return XcUser
     */
    @Transactional
    public XcUser addWxUser(Map<String, String> userInfo) {
        String unionid = userInfo.get("unionid");
        // 根据 unionid 查询数据库
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionid));
        if (user != null) {
            return user;
        }
        // 保存用户信息
        String userId = UUID.randomUUID().toString();
        user = new XcUser();
        user.setId(userId);
        user.setWxUnionid(unionid);
        user.setNickname(userInfo.get("nickname"));
        user.setUserpic(userInfo.get("headimgurl"));
        user.setName(userInfo.get("nickname"));
        user.setUsername(unionid);
        user.setPassword(unionid);
        user.setUtype("101001"); // 学生类型
        user.setStatus("1"); // 用户状态
        user.setCreateTime(LocalDateTime.now());
        xcUserMapper.insert(user);

        // 授予权限
        XcUserRole userRole = new XcUserRole();
        userRole.setId(UUID.randomUUID().toString());
        userRole.setUserId(userId);
        userRole.setRoleId("17"); // 学生角色
        xcUserRoleMapper.insert(userRole);

        return user;
    }
}
