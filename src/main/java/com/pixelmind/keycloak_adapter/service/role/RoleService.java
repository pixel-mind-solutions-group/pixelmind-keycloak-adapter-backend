package com.pixelmind.keycloak_adapter.service.role;

import com.pixelmind.keycloak_adapter.dto.CommonResponseDTO;
import com.pixelmind.keycloak_adapter.dto.role.RealmRoleRequestDTO;

public interface RoleService {

    CommonResponseDTO createRealmRole(RealmRoleRequestDTO roleRequest);

    CommonResponseDTO updateRealmRole(RealmRoleRequestDTO roleRequest);

    CommonResponseDTO deleteRealmRole(String realmName, String roleName);
}
