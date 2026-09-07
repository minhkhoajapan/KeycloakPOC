package com.example.KeycloakPOC;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot アプリケーションの起動クラス。
 * ALB のダウンストリームに位置するサービスとして 8080 番ポートで起動する。
 */
@SpringBootApplication
public class KeycloakPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeycloakPocApplication.class, args);
	}

}
