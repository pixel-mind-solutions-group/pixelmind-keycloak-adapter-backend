package com.pixelmind.keycloak_adapter.dto.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientResponseDTO {
    private String id;
    private String name;
    private String description;
    private boolean active;
}
