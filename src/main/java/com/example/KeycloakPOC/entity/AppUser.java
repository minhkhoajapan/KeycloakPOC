package com.example.KeycloakPOC.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Keycloak の UserInfo から組み立てるユーザー情報の DTO。
 * `/test` のレスポンスボディ（JSON）としてそのままシリアライズされる。
 * identityToken〜email は Keycloak の標準クレーム、personalMessage はアプリ独自フィールドの例。
 */
@Data
@NoArgsConstructor
public class AppUser {
    private String identityToken;
    private String name;
    private String preferredUsername;
    private String givenName;
    private String familyName;
    private String email;
    private String personalMessage;
}
