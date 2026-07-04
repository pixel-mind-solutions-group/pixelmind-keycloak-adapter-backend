package com.pixelmind.keycloak_adapter.controller.realm;

import com.pixelmind.keycloak_adapter.dto.CommonResponseDTO;
import com.pixelmind.keycloak_adapter.dto.realm.RealmRequestDTO;
import com.pixelmind.keycloak_adapter.enums.PersistType;
import com.pixelmind.keycloak_adapter.service.realm.RealmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/keycloak-adapter/realm")
public class RealmController {

    private final RealmService realmService;

    @PostMapping(value = "")
    public ResponseEntity<CommonResponseDTO> realm(@RequestParam(value = "persistType") PersistType persistType,
                                                   @RequestBody RealmRequestDTO realmRequest) {
        log.info("REST Request - realm: persistType={}, realmName={}", persistType, realmRequest.getRealmName());
        return ResponseEntity.ok(realmService.realm(persistType, realmRequest));
    }

    @GetMapping(value = "/active")
    public ResponseEntity<CommonResponseDTO> getActiveRealmsWithClients() {
        log.info("REST Request - getActiveRealmsWithClients");
        return ResponseEntity.ok(realmService.getActiveRealmsWithClients());
    }
}
