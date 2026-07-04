package com.pixelmind.keycloak_adapter.controller.client;

import com.pixelmind.keycloak_adapter.dto.CommonResponseDTO;
import com.pixelmind.keycloak_adapter.dto.client.ClientRequestDTO;
import com.pixelmind.keycloak_adapter.dto.client.ApiPermissionRequestDTO;
import com.pixelmind.keycloak_adapter.service.client.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/keycloak-adapter/client")
public class ClientController {

    private final ClientService clientService;

    @PostMapping(value = "/{realmName}/create")
    public ResponseEntity<CommonResponseDTO> createClient(
            @PathVariable String realmName,
            @RequestBody ClientRequestDTO clientRequest) {
        log.info("REST Request - createClient: realmName={}, clientId={}", realmName, clientRequest.getClientId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.createClient(realmName, clientRequest));
    }

    @PutMapping(value = "/{realmName}/{clientId}/update")
    public ResponseEntity<CommonResponseDTO> updateClient(
            @PathVariable String realmName,
            @PathVariable String clientId,
            @RequestBody ClientRequestDTO clientRequest) {
        log.info("REST Request - updateClient: realmName={}, clientId={}", realmName, clientId);
        return ResponseEntity.ok(clientService.updateClient(realmName, clientId, clientRequest));
    }

    @PostMapping(value = "/api-permissions/create")
    public ResponseEntity<CommonResponseDTO> createApiPermissions(
            @RequestBody ApiPermissionRequestDTO apiPermissionRequest) {
        log.info("REST Request - createApiPermissions: realmInternalUUid={}, internalApplicationUuid={}, size={}", 
                apiPermissionRequest.getRealmInternalUUid(), apiPermissionRequest.getInternalApplicationUuid(), 
                apiPermissionRequest.getApiPermisisonName() != null ? apiPermissionRequest.getApiPermisisonName().size() : 0);
        return ResponseEntity.ok(clientService.createApiPermissions(apiPermissionRequest));
    }

    @DeleteMapping(value = "/api-permissions/delete")
    public ResponseEntity<CommonResponseDTO> deleteApiPermission(
            @RequestParam(value = "realmInternalUUid") String realmInternalUUid,
            @RequestParam(value = "internalApplicationUuid") String internalApplicationUuid,
            @RequestParam(value = "apiPermissionName") String apiPermissionName) {
        log.info("REST Request - deleteApiPermission: realmInternalUUid={}, internalApplicationUuid={}, permissionName={}", 
                realmInternalUUid, internalApplicationUuid, apiPermissionName);
        return ResponseEntity.ok(clientService.deleteApiPermission(realmInternalUUid, internalApplicationUuid, apiPermissionName));
    }
}
