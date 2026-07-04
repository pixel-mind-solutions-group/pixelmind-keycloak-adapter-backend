package com.pixelmind.keycloak_adapter.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppTokenRequestDTO {
    private String realmName;
    private String internalApplicationUuid;
    private String username;
    private String password;
}
