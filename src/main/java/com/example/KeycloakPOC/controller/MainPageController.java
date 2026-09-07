package com.example.KeycloakPOC.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * `/index` を表示するコントローラー。
 * 前身ブランチ（Keycloak ログイン画面での OIDC 認証）の名残で Thymeleaf テンプレート index.html を返す。
 * 本ブランチの ALB 模擬フローには直接関与しない。
 */
@Controller
public class MainPageController {

    @RequestMapping("/index")
    public String getIndex(Model model) {
        model.addAttribute("greetingMessage", "ページへようこそ");
        return "index";
    }
}
