package com.pixelmind.keycloak_adapter.dto.client;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClientRequestDTO {
    private String clientId;          // unique client name
    private String name;              // display name
    private String description;       // optional description
    private String rootUrl;           // e.g. http://localhost:3000
    private String baseUrl;           // e.g. /dashboard
    private boolean enabled;
    private boolean publicClient;     // false = confidential (has secret)
    private boolean standardFlowEnabled;
    private boolean directAccessGrantsEnabled;
    private List<String> redirectUris;    // e.g. ["http://localhost:3000/*"]
    private List<String> webOrigins;      // e.g. ["http://localhost:3000"]
}
