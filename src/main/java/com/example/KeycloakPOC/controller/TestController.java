package com.example.KeycloakPOC.controller;

import com.example.KeycloakPOC.entity.AppUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public AppUser testEndpoint(HttpServletRequest req) {
        AppUser appUser = (AppUser) req.getAttribute("appUser");
        return appUser;
    }
}
