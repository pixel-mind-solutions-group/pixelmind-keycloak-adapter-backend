package com.pixelmind.keycloak_adapter.service.client;

import com.pixelmind.keycloak_adapter.dto.CommonResponseDTO;
import com.pixelmind.keycloak_adapter.dto.client.ClientRequestDTO;

public interface ClientService {

    CommonResponseDTO createClient(String realmName, ClientRequestDTO clientRequest);

    CommonResponseDTO updateClient(String realmName, String clientId, ClientRequestDTO clientRequest);
}
