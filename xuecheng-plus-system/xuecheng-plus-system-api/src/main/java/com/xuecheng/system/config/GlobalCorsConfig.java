package com.xuecheng.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/26 16:25
 * @Description 解决跨域问题 -- 实现了跨域过滤器，在响应头添加Access-Control-Allow-Origin
 */
@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter getCorsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        //添加哪些http方法可以跨域，比如：GET,Post，（多个方法中间以逗号分隔），*号表示所有
        configuration.addAllowedMethod("*");
        //添加允许哪个请求进行跨域，*表示所有,可以具体指定http://localhost:8601 表示只允许http://localhost:8601/跨域
        configuration.addAllowedOriginPattern("*");
        //所有头信息全部放行
        configuration.addAllowedHeader("*");
        //允许跨域发送cookie
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
}
