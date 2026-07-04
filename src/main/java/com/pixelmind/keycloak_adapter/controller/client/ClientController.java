package com.pixelmind.keycloak_adapter.controller.client;

import com.pixelmind.keycloak_adapter.dto.CommonResponseDTO;
import com.pixelmind.keycloak_adapter.dto.client.ClientRequestDTO;
import com.pixelmind.keycloak_adapter.dto.client.ApiPermissionRequestDTO;
import com.pixelmind.keycloak_adapter.service.client.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/keycloak-adapter/client")
public class ClientController {

    private final ClientService clientService;

    @PostMapping(value = "/{realmName}/create")
    public ResponseEntity<CommonResponseDTO> createClient(
            @PathVariable String realmName,
            @RequestBody ClientRequestDTO clientRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.createClient(realmName, clientRequest));
    }

    @PutMapping(value = "/{realmName}/{clientId}/update")
    public ResponseEntity<CommonResponseDTO> updateClient(
            @PathVariable String realmName,
            @PathVariable String clientId,
            @RequestBody ClientRequestDTO clientRequest) {
        return ResponseEntity.ok(clientService.updateClient(realmName, clientId, clientRequest));
    }

    @PostMapping(value = "/api-permissions/create")
    public ResponseEntity<CommonResponseDTO> createApiPermissions(
            @RequestBody ApiPermissionRequestDTO apiPermissionRequest) {
        return ResponseEntity.ok(clientService.createApiPermissions(apiPermissionRequest));
    }

    @DeleteMapping(value = "/api-permissions/delete")
    public ResponseEntity<CommonResponseDTO> deleteApiPermission(
            @RequestParam(value = "realmInternalUUid") String realmInternalUUid,
            @RequestParam(value = "internalApplicationUuid") String internalApplicationUuid,
            @RequestParam(value = "apiPermissionName") String apiPermissionName) {
        return ResponseEntity.ok(clientService.deleteApiPermission(realmInternalUUid, internalApplicationUuid, apiPermissionName));
    }
}
