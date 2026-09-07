package com.example.KeycloakPOC.controller;

import com.example.KeycloakPOC.entity.AppUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ダウンストリームの動作確認用エンドポイント `/test` を提供する REST コントローラー。
 * {@link AlbAuthInterceptor} がリクエスト属性 "appUser" に格納した {@link AppUser} を取り出し、
 * そのまま JSON レスポンスとして返す。
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public AppUser testEndpoint(HttpServletRequest req) {
        AppUser appUser = (AppUser) req.getAttribute("appUser");
        return appUser;
    }
}
