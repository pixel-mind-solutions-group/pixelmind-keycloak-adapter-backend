package com.pixelmind.keycloak_adapter.service.client;

import com.pixelmind.keycloak_adapter.dto.CommonResponseDTO;
import com.pixelmind.keycloak_adapter.dto.client.ClientRequestDTO;
import com.pixelmind.keycloak_adapter.dto.client.ApiPermissionRequestDTO;

public interface ClientService {

    CommonResponseDTO createClient(String realmName, ClientRequestDTO clientRequest);

    CommonResponseDTO updateClient(String realmName, String clientId, ClientRequestDTO clientRequest);

    CommonResponseDTO createApiPermissions(ApiPermissionRequestDTO request);

    CommonResponseDTO deleteApiPermission(String realmInternalUUid, String internalApplicationUuid, String apiPermissionName);
}
