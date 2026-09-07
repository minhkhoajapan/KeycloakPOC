package com.example.KeycloakPOC.utility;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AWS ALB（OIDC 認証）を模擬するローカル実行用のスタンドアロンクラス。
 * Direct Access Grant で Keycloak からアクセストークンを取得し、
 * ALB と同じヘッダー（x-amzn-oidc-accesstoken / x-amzn-oidc-identity）を付与して
 * ダウンストリームの `/test` を呼び出す。main メソッドから単体で実行する。
 */
public class AlbSimulator {
    private static final String KEYCLOAK_PATH = "http://localhost:9090";
    private static final String REALM = "test";
    private static final String USERNAME = "khoa";
    private static final String PASSWORD = "khoa";
    private static final String CLIENT_ID = "testclient";
    private static final String CLIENT_SECRET = "sQOBZqZmRH22GFShxQOdJBqQlubXSGsQ";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient http = HttpClient.newHttpClient();
    private static final String TEST_ENDPOINT_PATH = "http://localhost:8080/test";

    public static void main(String[] args) throws Exception {
        String token = getToken();
        JsonNode claims = decodeJwtPayload(token);
        //System.out.println(claims.toPrettyString());
        callEndpoint(token, claims.get("sub").asString());
    }

    static void callEndpoint(String accessToken, String sub) throws Exception{
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(TEST_ENDPOINT_PATH))
                .header("x-amzn-oidc-accesstoken", accessToken)
                .header("x-amzn-oidc-identity", sub)
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("--- callEndpoint response ---");
        System.out.println(res.body());
    }

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
        if (res.statusCode() != 200) {
            throw new RuntimeException("token call failed: " + res.statusCode() + " " + res.body());
        }

        return mapper.readTree(res.body()).get("access_token").asString();
    }

    static JsonNode decodeJwtPayload(String jwt) throws Exception {
        byte[] json = Base64.getUrlDecoder().decode(jwt.split("\\.")[1]);
        return mapper.readTree(json);
    }
}
