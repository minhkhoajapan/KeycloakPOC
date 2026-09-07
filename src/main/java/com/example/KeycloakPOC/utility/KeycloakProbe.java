package com.example.KeycloakPOC.utility;

import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class KeycloakProbe {
    private static final String KEYCLOAK_PATH = "http://localhost:9090";
    private static final String REALM = "test";
    private static final String USERNAME = "khoa";
    private static final String PASSWORD = "khoa";
    private static final String CLIENT_ID = "testclient";
    private static final String CLIENT_SECRET = "sQOBZqZmRH22GFShxQOdJBqQlubXSGsQ";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient http = HttpClient.newHttpClient();

    static String getToken() throws Exception {
        String form = "grant_type=password"
                + "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8)
                + "&username=" + URLEncoder.encode(USERNAME, StandardCharsets.UTF_8)
                + "&password=" + URLEncoder.encode(PASSWORD, StandardCharsets.UTF_8)
                + "&scope=openid";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(KEYCLOAK_PATH + "/realms/" + REALM + "/protocol/openid-connect/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        Object parsed = mapper.readValue(res.body(), Object.class);
        System.out.println("--- response body ---");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed));

        return mapper.readTree(res.body()).get("access_token").asString();
    }

    public static void main(String[] args) throws Exception {
        String token = getToken();
        System.out.println(token);
        // tokenでユーザー情報取得
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(KEYCLOAK_PATH + "/realms/" + REALM + "/protocol/openid-connect/userinfo"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        Object parsed = mapper.readValue(res.body(), Object.class);
        System.out.println("--- ユーザー情報取得 ---");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed));
    }
}
