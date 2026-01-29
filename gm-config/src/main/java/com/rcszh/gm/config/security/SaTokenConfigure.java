package com.rcszh.gm.config.security;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
                    String path = SaHolder.getRequest().getRequestPath();
                    String method = SaHolder.getRequest().getMethod();

                    // Public endpoints
                    if ("/api/auth/login".equals(path)
                            || "/api/account/login".equals(path)
                            || "/api/account/register".equals(path)
                            || "/error".equals(path)) {
                        return;
                    }

                    // Public: OW read endpoints (community list/detail included).
                    if (path.startsWith("/api/ow/") && "GET".equalsIgnoreCase(method)) {
                        return;
                    }

                    // Public: static files.
                    if (path.startsWith("/files/") && "GET".equalsIgnoreCase(method)) {
                        return;
                    }

                    // Everything else requires login.
                    StpUtil.checkLogin();
                }))
                .addPathPatterns("/**");
    }
}
