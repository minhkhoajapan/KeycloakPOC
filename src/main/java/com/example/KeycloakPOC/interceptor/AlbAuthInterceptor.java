package com.example.KeycloakPOC.interceptor;

import com.example.KeycloakPOC.entity.AppUser;
import com.example.KeycloakPOC.utility.KeycloakClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * ALB が付与するヘッダーを前提とした認証インターセプター。
 * リクエストヘッダー {@code x-amzn-oidc-accesstoken} からアクセストークンを取り出し、
 * {@link KeycloakClient} で Keycloak の UserInfo を取得して {@link AppUser} を組み立て、
 * リクエスト属性 "appUser" に格納してダウンストリームのコントローラーへ引き渡す。
 */
@Component
@RequiredArgsConstructor
public class AlbAuthInterceptor implements HandlerInterceptor {
    private static final String ACCESS_TOKEN_HEADER = "x-amzn-oidc-accesstoken";
    private static final String IDENTITY_TOKEN_HEADER = "x-amzn-oidc-identity";

    @Autowired
    private final KeycloakClient keycloakClient;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception{
        String accessToken = (String) req.getHeader(ACCESS_TOKEN_HEADER);
        AppUser appUser = keycloakClient.getUserInfo(accessToken);
        req.setAttribute("appUser", appUser);
        return true;
    }
}
