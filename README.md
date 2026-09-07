# KeycloakPOC（`albheader` ブランチ）

AWS ALB の **OIDC 認証** を模擬する PoC です。

ALB が「ユーザー認証 → トークン取得 → ダウンストリームへヘッダーで受け渡し」を担う構成を、
ローカル環境で再現します。ダウンストリームのコントローラーは、受け取ったトークンで
Keycloak から UserInfo を取得し、`AppUser` オブジェクトを JSON で返します。

> **補足**: このブランチでは前身の Spring Security（`oauth2Login`）による認証は使いません。
> 認証は「ALB がヘッダーを付与済み」という前提で、`AlbAuthInterceptor` がトークンを受け取る形に置き換えています。

## 全体の流れ

```
[AlbSimulator]                         [Spring Boot アプリ (:8080)]           [Keycloak (:9090)]
  1. Direct Access Grant でトークン取得 ──────────────────────────────────────▶ /token
  2. /test を呼び出し                     ┌───────────────────────────────┐
     ヘッダーを付与:                       │ AlbAuthInterceptor            │
       x-amzn-oidc-accesstoken  ─────────▶│  3. アクセストークンを取り出す  │
       x-amzn-oidc-identity               │  4. KeycloakClient で          │
                                          │     UserInfo を取得 ───────────┼──▶ /userinfo
                                          │  5. AppUser を組み立て          │
                                          │     req 属性 "appUser" に格納   │
                                          └──────────────┬────────────────┘
                                                         ▼
                                          ┌───────────────────────────────┐
                                          │ TestController (/test)        │
                                          │  6. appUser を JSON で返す     │
                                          └───────────────────────────────┘
```

各クラスの役割:

| クラス | 役割 |
| --- | --- |
| `AlbSimulator` | **ALB を模擬**。Keycloak からトークンを取得し、ALB と同じヘッダーを付けて `/test` を呼ぶ。単体実行用。 |
| `AlbAuthInterceptor` | 全リクエストをインターセプトし、アクセストークンヘッダーから UserInfo を取得して `AppUser` を組み立て、リクエスト属性に格納する。 |
| `KeycloakClient` | Keycloak の `/userinfo` を呼び出し、クレームを `AppUser` にマッピングする Spring コンポーネント。 |
| `TestController` | `/test` エンドポイント。リクエスト属性の `AppUser` を JSON で返す。 |
| `AppUser` | レスポンスとして返すユーザー情報の DTO。 |
| `WebConfig` | `AlbAuthInterceptor` を Spring MVC に登録する。 |
| `KeycloakProbe` | Keycloak のレスポンス構造を確認するための調査用スタンドアロンクラス（アプリ本体のフロー外）。 |
| `MainPageController` | 前身ブランチの名残（`/index` の Thymeleaf 表示）。本フローには関与しない。 |

## 技術スタック

| 項目 | 内容 |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.1.1（spring-boot-starter-webmvc / thymeleaf / jdbc） |
| HTTP クライアント | Java 標準 `java.net.http.HttpClient` |
| JSON | Jackson |
| IdP | Keycloak 26.0 |
| DB | PostgreSQL 16（Keycloak 用） |

## ヘッダー仕様

`AlbSimulator` が付与し、`AlbAuthInterceptor` が受け取るヘッダー（実際の ALB と同名）:

| ヘッダー | 内容 | 現状の利用 |
| --- | --- | --- |
| `x-amzn-oidc-accesstoken` | アクセストークン（JWT） | `AlbAuthInterceptor` が UserInfo 取得に使用 |
| `x-amzn-oidc-identity` | ユーザー識別子（`sub`） | 送信のみ（インターセプターでは未使用） |

## 起動手順

### 1. Keycloak と PostgreSQL を起動

```bash
docker compose up -d
```

- Keycloak 管理コンソール: http://localhost:9090 （admin / admin）
- PostgreSQL: `localhost:5433`

### 2. Keycloak 側の設定

Realm・Client・User を以下の値で用意します（`AlbSimulator` / `KeycloakClient` の定数と一致させること）:

- Realm: `test`
- Client: `testclient`
  - **Client authentication: ON**（confidential）
  - **Direct access grants: ON**（OFF だとトークン取得が失敗）
  - Credentials タブの Client secret を `AlbSimulator` / `KeycloakProbe` の `CLIENT_SECRET` に設定
- User: `khoa`（パスワード `khoa`）

### 3. Spring Boot アプリを起動

```bash
./mvnw spring-boot:run
```

### 4. ALB 模擬リクエストを実行

`AlbSimulator` の `main` を実行すると、トークン取得 → `/test` 呼び出し → レスポンス表示までを行います。

## レスポンス

現状（カスタムフィールド追加前）の `/test` レスポンス:

```json
{
  "identityToken": "c258b3d6-8345-4e8b-a0c8-317485c66413",
  "name": "Khoa Nguyen",
  "preferredUsername": null,
  "givenName": "Khoa",
  "familyName": "Nguyen",
  "email": "fakekhoaemail@gmail.com",
  "personalMessage": "fuck yeah it is working!"
}
```

- `identityToken`〜`email` は Keycloak の UserInfo（標準クレーム）由来。
- `preferredUsername` は現状マッピングしていないため `null`。
- `personalMessage` はアプリ側で付与している固定値（独自フィールドの例）。

> **今後の予定**: Keycloak の Client に **User Attribute マッパー**（Add to userinfo = ON）を追加し、
> 部署・社員番号などのカスタムフィールドを UserInfo に載せる。その際、拡張後のレスポンス例を本 README に追記する。

## メモ / 注意点

- トークンリクエストには `scope=openid` を含める（含めないと `id_token` が発行されず、`/token` が 403 を返す）。
- `Authorization` ヘッダーは `"Bearer " + token`。`Bearer` の後ろのスペースを忘れると `/userinfo` が空ボディを返す。
</content>
</invoke>
