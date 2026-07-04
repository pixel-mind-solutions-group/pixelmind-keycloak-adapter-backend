package com.pixelmind.keycloak_adapter.controller.auth;

import com.pixelmind.keycloak_adapter.dto.CommonResponseDTO;
import com.pixelmind.keycloak_adapter.dto.auth.TokenRequestDTO;
import com.pixelmind.keycloak_adapter.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixelmind.keycloak_adapter.dto.auth.AppTokenRequestDTO;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/keycloak-adapter/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/token")
    public ResponseEntity<CommonResponseDTO> getToken(@RequestBody TokenRequestDTO tokenRequest) {
        log.info("REST Request - getToken: realmName={}, clientId={}, username={}", 
                tokenRequest.getRealmName(), tokenRequest.getClientId(), tokenRequest.getUsername());
        return ResponseEntity.ok(authService.getToken(tokenRequest));
    }

    @PostMapping(value = "/token/app")
    public ResponseEntity<CommonResponseDTO> getAppToken(@RequestBody AppTokenRequestDTO appTokenRequest) {
        log.info("REST Request - getAppToken: realmName={}, internalApplicationUuid={}, username={}", 
                appTokenRequest.getRealmName(), appTokenRequest.getInternalApplicationUuid(), appTokenRequest.getUsername());
        return ResponseEntity.ok(authService.getAppToken(appTokenRequest));
    }
}
