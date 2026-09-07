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
        appUser.setIdentityToken(appUserJsonNode.get("sub").asString());
        appUser.setName(appUserJsonNode.get("name").asString());
        appUser.setPreferredUsername(appUserJsonNode.get("preferred_username").asString());
        appUser.setGivenName(appUserJsonNode.get("given_name").asString());
        appUser.setFamilyName(appUserJsonNode.get("family_name").asString());
        appUser.setEmail(appUserJsonNode.get("email").asString());
        appUser.setPersonalMessage("はじめまして。よろしくお願いします");
        appUser.setOriginCountry(appUserJsonNode.get("origin_country").asString());
        appUser.setKanjiName(appUserJsonNode.get("kanji_name").asString());

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
