# KeycloakPOC

Spring Boot アプリケーションを **Keycloak** で OIDC (OpenID Connect) 認証する PoC です。
未認証で保護ページ (`/index`) にアクセスすると Keycloak のログイン画面へリダイレクトされ、
ログイン成功後に `/index` がレンダリングされることを確認します。

## 技術スタック

| 項目 | 内容 |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.1.1 (spring-boot-starter-security-oauth2-client) |
| テンプレート | Thymeleaf |
| IdP | Keycloak 26.0 |
| DB | PostgreSQL 16 (Keycloak 用 / アプリ用) |

## アーキテクチャ / 構成

- **Spring Boot アプリ**: `localhost:8080`
  - `SecurityConfig` で全リクエストを認証必須にし、`oauth2Login` を有効化。ログイン成功後は `/index` へ遷移。
  - `MainPageController` が `/index` で Thymeleaf テンプレート `index.html` を返す。
- **Keycloak**: `localhost:9090`（コンテナ内 8080 → ホスト 9090）
  - realm: `keycloakpoc`
  - client: `keycloakpocapp`
- **PostgreSQL**: `localhost:5433`
  - Keycloak 用 DB: `keycloak`
  - アプリ用 DB: `kcdb`（`init-db/01-create-kcdb.sql` で作成）

## 主な設定値（`src/main/resources/application.properties`）

```properties
server.port=8080

spring.security.oauth2.client.registration.keycloak.client-id=keycloakpocapp
spring.security.oauth2.client.registration.keycloak.scope=openid,profile,email
spring.security.oauth2.client.registration.keycloak.authorization-grant-type=authorization_code
spring.security.oauth2.client.provider.keycloak.issuer-uri=http://localhost:9090/realms/keycloakpoc
```


## 起動手順

### 1. Keycloak と PostgreSQL を起動

```bash
docker compose up -d
```

- Keycloak 管理コンソール: http://localhost:9090 （admin / admin）
- PostgreSQL: `localhost:5433`

### 2. Keycloak 側の初期設定

管理コンソールで以下を作成します（下記スクリーンショット参照）。

1. Realm `keycloakpoc` を作成
2. Client `keycloakpocapp` を作成
   - Client authentication: ON（confidential）
   - Valid redirect URIs: `http://localhost:8080/login/oauth2/code/keycloak`
   - Credentials タブの Client secret を `application.properties` に設定
3. User を作成し、パスワードを設定（Credentials タブ）

### 3. Spring Boot アプリを起動

```bash
./mvnw spring-boot:run
```

## 動作確認（デモ）

### 1. 未認証で `/index` にアクセス → Keycloak ログイン画面へリダイレクト

ブラウザで http://localhost:8080/index を開くと、Keycloak のログイン画面へリダイレクトされます。

![未認証アクセス時のログインリダイレクト](docs/images/01-redirect-to-login.png)

### 2. ログイン成功後、`/index` が表示される

作成したユーザーでログインすると `/index` に戻り、`ページへようこそ` が表示されます。

![ログイン後の index ページ](docs/images/02-index-after-login.png)

## Keycloak 設定のスクリーンショット

### Client 設定（`keycloakpocapp`）

![Keycloak Client 設定](docs/images/03-keycloak-client.png)

### User 設定

![Keycloak User 設定](docs/images/04-keycloak-user.png)

---

