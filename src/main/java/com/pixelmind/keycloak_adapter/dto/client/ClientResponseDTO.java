package com.pixelmind.keycloak_adapter.dto.client;

import com.pixelmind.keycloak_adapter.dto.realm.RealmResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientResponseDTO {
    private String id;
    private String clientId;
    private String name;
    private String description;
    private boolean active;
    private RealmResponseDTO realm;
}
