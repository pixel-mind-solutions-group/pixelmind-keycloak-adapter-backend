package com.pixelmind.keycloak_adapter.dto.role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealmRoleRequestDTO {
    private String realmName;
    private String name;
    private String newName;
    private String description;
}
