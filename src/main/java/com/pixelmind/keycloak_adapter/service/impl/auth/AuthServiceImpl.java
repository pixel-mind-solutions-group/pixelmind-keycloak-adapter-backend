package com.pixelmind.keycloak_adapter.service.impl.auth;

import com.pixelmind.keycloak_adapter.constant.TokenConstant;
import com.pixelmind.keycloak_adapter.dto.CommonResponseDTO;
import com.pixelmind.keycloak_adapter.dto.auth.TokenRequestDTO;
import com.pixelmind.keycloak_adapter.dto.auth.AppTokenRequestDTO;
import com.pixelmind.keycloak_adapter.exception.BaseException;
import com.pixelmind.keycloak_adapter.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RestClient restClient;
    private final Keycloak keycloak;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Override
    public CommonResponseDTO getToken(TokenRequestDTO tokenRequest) {
        log.info("AuthServiceImpl => getToken: realm={}, clientId={}, username={}",
                tokenRequest.getRealmName(), tokenRequest.getClientId(), tokenRequest.getUsername());

        // ── Build token URL ───────────────────────────────────────
        String tokenUrl = serverUrl + "/realms/"
                + tokenRequest.getRealmName()
                + "/protocol/openid-connect/token";

        // ── Build form body manually ──────────────────────────────
        String formBody = "grant_type=password"
                + "&client_id=" + tokenRequest.getClientId()
                + "&client_secret=" + tokenRequest.getClientSecret()
                + "&username=" + tokenRequest.getUsername()
                + "&password=" + tokenRequest.getPassword();

        try {
            log.info("Sending request to Keycloak token endpoint: {}", tokenUrl);
            Map response = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formBody)
                    .retrieve()
                    .body(Map.class);

            // Extract token data
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("accessToken", response.get(TokenConstant.ACESS_TOKEN));
            tokenData.put("refreshToken", response.get(TokenConstant.REFRESH_TOKEN));
            tokenData.put("expiresIn", response.get(TokenConstant.EXPIRES_IN));
            tokenData.put("tokenType", response.get(TokenConstant.TOKEN_TYPE));

            log.info("Token generated successfully for user: {} in realm: {}", tokenRequest.getUsername(), tokenRequest.getRealmName());
            return new CommonResponseDTO(
                    HttpStatus.OK.value(),
                    tokenData,
                    "Token generated successfully"
            );

        } catch (Exception e) {
            log.error("Authentication failed for username: {} in realm: {}. Error: {}", 
                    tokenRequest.getUsername(), tokenRequest.getRealmName(), e.getMessage(), e);
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "Authentication failed: " + e.getMessage());
        }
    }

    @Override
    public CommonResponseDTO getAppToken(AppTokenRequestDTO request) {
        log.info("AuthServiceImpl => getAppToken: realm={}, internalApplicationUuid={}, username={}",
                request.getRealmName(), request.getInternalApplicationUuid(), request.getUsername());

        try {
            // 1. Fetch Client representation from Keycloak using internalApplicationUuid
            ClientRepresentation client = keycloak.realm(request.getRealmName())
                    .clients()
                    .get(request.getInternalApplicationUuid())
                    .toRepresentation();

            String clientId = client.getClientId();
            String clientSecret = null;
            if (client.isPublicClient() != null && !client.isPublicClient()) {
                CredentialRepresentation secretRep = keycloak.realm(request.getRealmName())
                        .clients()
                        .get(request.getInternalApplicationUuid())
                        .getSecret();
                clientSecret = secretRep.getValue();
            }

            // 2. Build token URL
            String tokenUrl = serverUrl + "/realms/"
                    + request.getRealmName()
                    + "/protocol/openid-connect/token";

            // 3. Build form body
            StringBuilder formBodyBuilder = new StringBuilder();
            formBodyBuilder.append("grant_type=password")
                    .append("&client_id=").append(clientId)
                    .append("&username=").append(request.getUsername())
                    .append("&password=").append(request.getPassword());
            
            if (clientSecret != null) {
                formBodyBuilder.append("&client_secret=").append(clientSecret);
            }

            String formBody = formBodyBuilder.toString();

            log.info("Sending request to Keycloak token endpoint: {}", tokenUrl);
            Map response = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formBody)
                    .retrieve()
                    .body(Map.class);

            // Extract token data
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("accessToken", response.get(TokenConstant.ACESS_TOKEN));
            tokenData.put("refreshToken", response.get(TokenConstant.REFRESH_TOKEN));
            tokenData.put("expiresIn", response.get(TokenConstant.EXPIRES_IN));
            tokenData.put("tokenType", response.get(TokenConstant.TOKEN_TYPE));

            log.info("Token generated successfully for user: {} in realm: {}", request.getUsername(), request.getRealmName());
            return new CommonResponseDTO(
                    HttpStatus.OK.value(),
                    tokenData,
                    "Token generated successfully"
            );

        } catch (Exception e) {
            log.error("App Authentication failed for username: {} in realm: {}. Error: {}", 
                    request.getUsername(), request.getRealmName(), e.getMessage(), e);
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "Authentication failed: " + e.getMessage());
        }
    }
}
