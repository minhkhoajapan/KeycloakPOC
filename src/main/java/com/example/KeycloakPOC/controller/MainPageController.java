package com.example.KeycloakPOC.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainPageController {

    @RequestMapping("/index")
    public String getIndex(Model model) {
        model.addAttribute("greetingMessage", "ページへようこそ");
        return "index";
    }
}
