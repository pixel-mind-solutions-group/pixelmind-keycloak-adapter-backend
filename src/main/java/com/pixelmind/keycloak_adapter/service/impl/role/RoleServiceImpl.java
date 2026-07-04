package com.pixelmind.keycloak_adapter.service.impl.role;

import com.pixelmind.keycloak_adapter.dto.CommonResponseDTO;
import com.pixelmind.keycloak_adapter.dto.role.RealmRoleRequestDTO;
import com.pixelmind.keycloak_adapter.service.role.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final Keycloak keycloak;

    @Override
    public CommonResponseDTO createRealmRole(RealmRoleRequestDTO roleRequest) {
        log.info("RoleServiceImpl => createRealmRole: realm={}, role={}", 
                roleRequest.getRealmName(), roleRequest.getName());

        try {
            boolean realmExists = keycloak.realms().findAll().stream()
                    .anyMatch(r -> r.getRealm().equalsIgnoreCase(roleRequest.getRealmName()));
            if (!realmExists) {
                return new CommonResponseDTO(
                        HttpStatus.NOT_FOUND.value(),
                        null,
                        "Realm not found: " + roleRequest.getRealmName()
                );
            }

            List<RoleRepresentation> existingRoles = keycloak.realm(roleRequest.getRealmName()).roles().list();
            boolean roleExists = existingRoles.stream()
                    .anyMatch(r -> r.getName().equalsIgnoreCase(roleRequest.getName()));
            if (roleExists) {
                return new CommonResponseDTO(
                        HttpStatus.CONFLICT.value(),
                        null,
                        "Realm role already exists: " + roleRequest.getName()
                );
            }

            RoleRepresentation role = new RoleRepresentation();
            role.setName(roleRequest.getName());
            role.setDescription(roleRequest.getDescription());
            role.setClientRole(false);

            keycloak.realm(roleRequest.getRealmName()).roles().create(role);

            return new CommonResponseDTO(
                    HttpStatus.CREATED.value(),
                    null,
                    "Realm role created successfully"
            );

        } catch (Exception e) {
            log.error("Error creating realm role in Keycloak: {}", e.getMessage());
            return new CommonResponseDTO(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null,
                    "Unexpected error: " + e.getMessage()
            );
        }
    }

    @Override
    public CommonResponseDTO updateRealmRole(RealmRoleRequestDTO roleRequest) {
        log.info("RoleServiceImpl => updateRealmRole: realm={}, oldRole={}, newRole={}", 
                roleRequest.getRealmName(), roleRequest.getName(), roleRequest.getNewName());

        try {
            List<RoleRepresentation> existingRoles = keycloak.realm(roleRequest.getRealmName()).roles().list();
            boolean oldRoleExists = existingRoles.stream()
                    .anyMatch(r -> r.getName().equals(roleRequest.getName()));
            if (!oldRoleExists) {
                return new CommonResponseDTO(
                        HttpStatus.NOT_FOUND.value(),
                        null,
                        "Realm role not found: " + roleRequest.getName()
                );
            }

            if (roleRequest.getNewName() != null && !roleRequest.getNewName().equals(roleRequest.getName())) {
                boolean newRoleExists = existingRoles.stream()
                        .anyMatch(r -> r.getName().equalsIgnoreCase(roleRequest.getNewName()));
                if (newRoleExists) {
                    return new CommonResponseDTO(
                            HttpStatus.CONFLICT.value(),
                            null,
                            "Role name already exists: " + roleRequest.getNewName()
                    );
                }
            }

            RoleRepresentation role = keycloak.realm(roleRequest.getRealmName()).roles().get(roleRequest.getName()).toRepresentation();
            if (roleRequest.getNewName() != null) {
                role.setName(roleRequest.getNewName());
            }
            if (roleRequest.getDescription() != null) {
                role.setDescription(roleRequest.getDescription());
            }

            keycloak.realm(roleRequest.getRealmName()).roles().get(roleRequest.getName()).update(role);

            return new CommonResponseDTO(
                    HttpStatus.OK.value(),
                    null,
                    "Realm role updated successfully"
                );

        } catch (Exception e) {
            log.error("Error updating realm role in Keycloak: {}", e.getMessage());
            return new CommonResponseDTO(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null,
                    "Unexpected error: " + e.getMessage()
            );
        }
    }

    @Override
    public CommonResponseDTO deleteRealmRole(String realmName, String roleName) {
        log.info("RoleServiceImpl => deleteRealmRole: realm={}, role={}", realmName, roleName);

        try {
            List<RoleRepresentation> existingRoles = keycloak.realm(realmName).roles().list();
            boolean roleExists = existingRoles.stream()
                    .anyMatch(r -> r.getName().equals(roleName));
            if (!roleExists) {
                return new CommonResponseDTO(
                        HttpStatus.NOT_FOUND.value(),
                        null,
                        "Realm role not found: " + roleName
                );
            }

            keycloak.realm(realmName).roles().deleteRole(roleName);

            return new CommonResponseDTO(
                    HttpStatus.OK.value(),
                    null,
                    "Realm role deleted successfully"
            );

        } catch (Exception e) {
            log.error("Error deleting realm role in Keycloak: {}", e.getMessage());
            return new CommonResponseDTO(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null,
                    "Unexpected error: " + e.getMessage()
            );
        }
    }
}
