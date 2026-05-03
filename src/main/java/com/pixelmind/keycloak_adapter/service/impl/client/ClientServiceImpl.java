package com.pixelmind.keycloak_adapter.service.impl.client;

import com.pixelmind.keycloak_adapter.dto.CommonResponseDTO;
import com.pixelmind.keycloak_adapter.dto.client.ClientRequestDTO;
import com.pixelmind.keycloak_adapter.service.client.ClientService;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final Keycloak keycloak;

    // ── Create Client ─────────────────────────────────────────────────────────
    @Override
    public CommonResponseDTO createClient(String realmName, ClientRequestDTO clientRequest) {
        String clientUUID = null;
        try {
            // Step 1: Check if client already exists
            List<ClientRepresentation> existing = keycloak
                    .realm(realmName)
                    .clients()
                    .findByClientId(clientRequest.getClientId());

            if (existing != null && !existing.isEmpty()) {
                return new CommonResponseDTO(
                        HttpStatus.CONFLICT.value(),
                        null,
                        "Client already exists: " + clientRequest.getClientId()
                );
            }

            // Step 2: Build client representation
            ClientRepresentation client = buildClientRepresentation(clientRequest);

            // Step 3: Create client in Keycloak
            Response response = keycloak
                    .realm(realmName)
                    .clients()
                    .create(client);

            if (response.getStatus() != 201) {
                return new CommonResponseDTO(
                        response.getStatus(),
                        null,
                        "Failed to create client in Keycloak"
                );
            }

            // Step 4: Extract client UUID from Location header
            String location = response.getHeaderString("Location");
            clientUUID = location.substring(location.lastIndexOf("/") + 1);

            // Step 5: Fetch created client secret (only for confidential clients)
            String clientSecret = null;
            if (!clientRequest.isPublicClient()) {
                CredentialRepresentation secret = keycloak
                        .realm(realmName)
                        .clients()
                        .get(clientUUID)
                        .getSecret();
                clientSecret = secret.getValue();
            }

            // Step 6: Save to DB via mapper
            // clientRepository.save(clientMapper.toEntity(clientRequest, clientUUID, clientSecret));

            // Step 7: Build response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("clientUUID", clientUUID);
            responseData.put("clientId", clientRequest.getClientId());
            responseData.put("clientSecret", clientSecret != null ? clientSecret : "N/A (public client)");

            return new CommonResponseDTO(
                    HttpStatus.CREATED.value(),
                    responseData,
                    "Client created successfully"
            );

        } catch (NotFoundException e) {
            rollbackClientCreation(realmName, clientUUID);
            return new CommonResponseDTO(
                    HttpStatus.NOT_FOUND.value(),
                    null,
                    "Realm not found: " + realmName
            );
        } catch (Exception e) {
            rollbackClientCreation(realmName, clientUUID);
            return new CommonResponseDTO(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null,
                    "Unexpected error: " + e.getMessage()
            );
        }
    }

    // ── Update Client ─────────────────────────────────────────────────────────
    @Override
    public CommonResponseDTO updateClient(String realmName, String clientId, ClientRequestDTO clientRequest) {
        try {
            // Step 1: Find client UUID by clientId
            ClientRepresentation existing = keycloak
                    .realm(realmName)
                    .clients()
                    .findByClientId(clientId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Client not found: " + clientId));

            String clientUUID = existing.getId();

            // Step 2: Fetch full current representation
            ClientRepresentation client = keycloak
                    .realm(realmName)
                    .clients()
                    .get(clientUUID)
                    .toRepresentation();

            // Step 3: Update only provided fields
            if (clientRequest.getName() != null) {
                client.setName(clientRequest.getName());
            }
            if (clientRequest.getDescription() != null) {
                client.setDescription(clientRequest.getDescription());
            }
            if (clientRequest.getRootUrl() != null) {
                client.setRootUrl(clientRequest.getRootUrl());
            }
            if (clientRequest.getBaseUrl() != null) {
                client.setBaseUrl(clientRequest.getBaseUrl());
            }
            if (clientRequest.getRedirectUris() != null) {
                client.setRedirectUris(clientRequest.getRedirectUris());
            }
            if (clientRequest.getWebOrigins() != null) {
                client.setWebOrigins(clientRequest.getWebOrigins());
            }
            client.setEnabled(clientRequest.isEnabled());
            client.setPublicClient(clientRequest.isPublicClient());
            client.setStandardFlowEnabled(clientRequest.isStandardFlowEnabled());
            client.setDirectAccessGrantsEnabled(clientRequest.isDirectAccessGrantsEnabled());

            // Step 4: Apply update
            keycloak
                    .realm(realmName)
                    .clients()
                    .get(clientUUID)
                    .update(client);

            return new CommonResponseDTO(
                    HttpStatus.OK.value(),
                    null,
                    "Client updated successfully: " + clientId
            );

        } catch (NotFoundException e) {
            return new CommonResponseDTO(
                    HttpStatus.NOT_FOUND.value(),
                    null,
                    "Realm or Client not found"
            );
        } catch (Exception e) {
            return new CommonResponseDTO(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null,
                    "Unexpected error: " + e.getMessage()
            );
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private ClientRepresentation buildClientRepresentation(ClientRequestDTO req) {
        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(req.getClientId());
        client.setName(req.getName());
        client.setDescription(req.getDescription());
        client.setRootUrl(req.getRootUrl());
        client.setBaseUrl(req.getBaseUrl());
        client.setEnabled(req.isEnabled());
        client.setPublicClient(req.isPublicClient());
        client.setStandardFlowEnabled(req.isStandardFlowEnabled());
        client.setDirectAccessGrantsEnabled(req.isDirectAccessGrantsEnabled());
        client.setRedirectUris(req.getRedirectUris());
        client.setWebOrigins(req.getWebOrigins());
        return client;
    }

    private void rollbackClientCreation(String realmName, String clientUUID) {
        try {
            if (clientUUID != null) {
                keycloak.realm(realmName).clients().get(clientUUID).remove();
                log.warn("Rollback: deleted client UUID {} from realm {}", clientUUID, realmName);
            }
        } catch (Exception e) {
            log.error("Rollback FAILED — manually delete clientUUID: {} in realm: {}", clientUUID, realmName);
        }
    }
}
