package com.pixelmind.keycloak_adapter.dto.client;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiPermissionRequestDTO {
    private String realmInternalUUid;
    private String internalApplicationUuid;
    private List<String> apiPermisisonName;
}
