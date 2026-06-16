package com.pixelmind.keycloak_adapter.dto.realm;

import com.pixelmind.keycloak_adapter.dto.client.ClientResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RealmResponseDTO {
    private String id;
    private String realmName;
    private String displayName;
    private Boolean enabled;
    private List<ClientResponseDTO> clients;
}
