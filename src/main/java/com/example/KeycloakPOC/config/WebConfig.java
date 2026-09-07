package com.example.KeycloakPOC.config;

import com.example.KeycloakPOC.interceptor.AlbAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC の設定クラス。
 * ALB を模擬した認証処理を全リクエストに適用するため、{@link AlbAuthInterceptor} を登録する。
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AlbAuthInterceptor albAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(albAuthInterceptor);
    }
}
