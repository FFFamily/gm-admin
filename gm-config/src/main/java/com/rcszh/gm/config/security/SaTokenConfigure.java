package com.rcszh.gm.config.security;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> SaRouter.match("/**")
                        .notMatch("/api/auth/login")
                        .notMatch("/api/account/login")
                        .notMatch("/api/account/register")
                        .notMatch("/api/ow/**")
                        .notMatch("/error")
                        .check(r -> StpUtil.checkLogin())))
                .addPathPatterns("/**");
    }
}
