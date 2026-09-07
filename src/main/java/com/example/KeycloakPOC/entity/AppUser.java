package com.example.KeycloakPOC.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

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
