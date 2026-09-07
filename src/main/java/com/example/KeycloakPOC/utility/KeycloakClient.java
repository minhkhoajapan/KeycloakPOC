package com.example.KeycloakPOC.utility;

import com.example.KeycloakPOC.entity.AppUser;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Keycloak の UserInfo エンドポイントを呼び出す専用クライアント。
 * アクセストークンで `/userinfo` を叩き、返ってきたクレームを {@link AppUser} にマッピングする。
 */
@Component
public class KeycloakClient {

    private static final String KEYCLOAK_PATH = "http://localhost:9090";
    private static final String REALM_TEST = "test";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient http = HttpClient.newHttpClient();

    public AppUser getUserInfo(String accessToken) throws Exception {
        JsonNode appUserJsonNode = getUserInfoJsonNode(accessToken);
        System.out.println(appUserJsonNode.toPrettyString());

        AppUser appUser = new AppUser();
        appUser.setIdentityToken(appUserJsonNode.path("sub").asString(null));
        appUser.setName(appUserJsonNode.path("name").asString(null));
        appUser.setPreferredUsername(appUserJsonNode.path("preferred_username").asString(null));
        appUser.setGivenName(appUserJsonNode.path("given_name").asString(null));
        appUser.setFamilyName(appUserJsonNode.path("family_name").asString(null));
        appUser.setEmail(appUserJsonNode.path("email").asString(null));
        appUser.setPersonalMessage("はじめまして。よろしくお願いします");
        appUser.setOriginCountry(appUserJsonNode.path("origin_country").asString(null));
        appUser.setKanjiName(appUserJsonNode.path("kanji_name").asString(null));

        return appUser;
    }

    private JsonNode getUserInfoJsonNode(String accessToken) throws Exception {
        String userInfoURI = KEYCLOAK_PATH + "/realms/" + REALM_TEST + "/protocol/openid-connect/userinfo";

        HttpRequest req =  HttpRequest.newBuilder()
                .uri(URI.create(userInfoURI))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(res.body());
    }
}
