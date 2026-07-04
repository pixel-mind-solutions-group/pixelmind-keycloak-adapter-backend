package com.pixelmind.keycloak_adapter.service.impl.realm;

import com.pixelmind.keycloak_adapter.constant.CommonConstant;
import com.pixelmind.keycloak_adapter.dto.CommonResponseDTO;
import com.pixelmind.keycloak_adapter.dto.realm.RealmRequestDTO;
import com.pixelmind.keycloak_adapter.enums.PersistType;
import com.pixelmind.keycloak_adapter.exception.BaseException;
import com.pixelmind.keycloak_adapter.mapper.realm.RealmMapper;
import com.pixelmind.keycloak_adapter.service.realm.RealmService;
import jakarta.ws.rs.ClientErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealmServiceImpl implements RealmService {

    private final Keycloak keycloak;
    private final RealmMapper realmMapper;

    @Override
    public CommonResponseDTO realm(PersistType persistType,
                                   RealmRequestDTO realmRequest) {
        log.info("RealmServiceImpl => realm: persistType={}, request={}", persistType, realmRequest);

        String message = null;
        RealmRepresentation realmRepresentation;

        try {
            switch (persistType) {

                case CREATE: {
                    log.info("Attempting to create realm: {}", realmRequest.getRealmName());
                    if (isRealmExists(realmRequest.getRealmName())) {
                        log.warn("Realm creation conflict: Realm already exists: {}", realmRequest.getRealmName());
                        return new CommonResponseDTO(
                                HttpStatus.CONFLICT.value(),
                                null,
                                "Realm already exists: " + realmRequest.getRealmName()
                        );
                    }

                    // Create new realm object
                    realmRepresentation = new RealmRepresentation();

                    realmRepresentation.setRealm(realmRequest.getRealmName());
                    realmRepresentation.setDisplayName(realmRequest.getDisplayName());
                    realmRepresentation.setEnabled(realmRequest.getEnabled());

                    keycloak.realms().create(realmRepresentation);
                    message = "Realm created: " + realmRequest.getRealmName();
                    log.info("Successfully created realm: {}", realmRequest.getRealmName());
                    break;

                }
                case UPDATE: {
                    log.info("Attempting to update realm: ID={}, Name={}", realmRequest.getId(), realmRequest.getRealmName());
                    if (!isRealmExists(realmRequest.getId())) {
                        log.warn("Realm update conflict: Realm does not exist: {}", realmRequest.getId());
                        return new CommonResponseDTO(
                                HttpStatus.CONFLICT.value(),
                                null,
                                "Realm is not exists: " + realmRequest.getId()
                        );
                    }

                    // Get the existing realm
                    RealmResource realmResource = keycloak.realm(realmRequest.getId());

                    // Fetch current representation
                    realmRepresentation = realmResource.toRepresentation();

                    realmRepresentation.setDisplayName(realmRequest.getDisplayName());
                    realmRepresentation.setRealm(realmRequest.getRealmName());
                    realmRepresentation.setEnabled(realmRequest.getEnabled());

                    realmResource.update(realmRepresentation);
                    message = "Realm updated: " + realmRequest.getRealmName();
                    log.info("Successfully updated realm ID={}", realmRequest.getId());
                    break;
                }
                case DELETE: {
                    log.info("Attempting to delete realm: {}", realmRequest.getId());
                    // Prevent deleting master realm
                    if (!isRealmExists(realmRequest.getId())) {
                        log.warn("Realm delete failure: Realm does not exist: {}", realmRequest.getId());
                        return new CommonResponseDTO(
                                HttpStatus.CONFLICT.value(),
                                null,
                                "Realm is not exists: " + realmRequest.getId()
                        );

                    } else if (realmRequest.getId().equalsIgnoreCase(CommonConstant.MASTER_REALM_NAME)) {
                        log.warn("Access Denied: Attempted to delete master realm");
                        return new CommonResponseDTO(
                                HttpStatus.FORBIDDEN.value(),
                                null,
                                "Cannot delete the master realm"
                        );
                    }

                    // Delete the realm
                    keycloak.realms().realm(realmRequest.getId()).remove();
                    message = "Realm deleted: " + realmRequest.getId();
                    log.info("Successfully deleted realm: {}", realmRequest.getId());
                }
            }

        } catch (ClientErrorException e) {
            log.error("ClientErrorException occurred during realm operation [{}]: {}", persistType, e.getMessage(), e);
            throw new BaseException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Keycloak client error: " + e.getMessage()
            );

        } catch (Exception e) {
            log.error("Unexpected error during realm operation [{}]: {}", persistType, e.getMessage(), e);
            throw new BaseException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal server error: " + e.getMessage()
            );
        }

        return new CommonResponseDTO(
                HttpStatus.OK.value(),
                null,
                message
        );
    }

    private boolean isRealmExists(String realmName) {
        log.debug("Checking if realm exists: {}", realmName);
        return keycloak.realms().findAll()
                .stream()
                .anyMatch(r -> r.getRealm().equalsIgnoreCase(realmName));
    }

    @Override
    public CommonResponseDTO getActiveRealmsWithClients() {
        log.info("RealmServiceImpl => getActiveRealmsWithClients accessed");

        try {
            List<RealmRepresentation> activeRealms = keycloak.realms().findAll().stream()
                    .filter(realmRepresentation -> realmRepresentation.isEnabled())
                    .collect(Collectors.toList());

            if (activeRealms.isEmpty()) {
                log.info("No active realms found in Keycloak");
                return new CommonResponseDTO(
                        HttpStatus.NO_CONTENT.value(),
                        List.of(),
                        "No realms found"
                );
            }

            log.info("Found {} active realms. Fetching clients for each realm...", activeRealms.size());
            for (RealmRepresentation realm : activeRealms) {
                try {
                    List<ClientRepresentation> clients = keycloak
                            .realm(realm.getRealm())
                            .clients()
                            .findAll();
                    realm.setClients(clients);
                    log.debug("Fetched {} clients for realm: {}", clients.size(), realm.getRealm());
                } catch (Exception e) {
                    log.error("Failed to fetch clients for realm: {}. Error: {}", realm.getRealm(), e.getMessage());
                    realm.setClients(Collections.emptyList());
                }
            }

            return new CommonResponseDTO(
                    HttpStatus.OK.value(),
                    realmMapper.toRealmResponseDTOs(activeRealms),
                    "Active realms with clients retrieved successfully"
            );

        } catch (Exception e) {
            log.error("Unexpected error while fetching active realms with clients: {}", e.getMessage(), e);
            throw new BaseException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error: " + e.getMessage()
            );
        }
    }
}
