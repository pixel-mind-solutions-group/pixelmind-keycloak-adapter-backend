package com.pixelmind.keycloak_adapter.controller.role;

import com.pixelmind.keycloak_adapter.dto.CommonResponseDTO;
import com.pixelmind.keycloak_adapter.dto.role.RealmRoleRequestDTO;
import com.pixelmind.keycloak_adapter.service.role.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/keycloak-adapter/role")
public class RoleController {

    private final RoleService roleService;

    @PostMapping(value = "/realm/create")
    public ResponseEntity<CommonResponseDTO> createRealmRole(@RequestBody RealmRoleRequestDTO roleRequest) {
        log.info("REST Request - createRealmRole: realm={}, role={}", roleRequest.getRealmName(), roleRequest.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roleService.createRealmRole(roleRequest));
    }

    @PutMapping(value = "/realm/update")
    public ResponseEntity<CommonResponseDTO> updateRealmRole(@RequestBody RealmRoleRequestDTO roleRequest) {
        log.info("REST Request - updateRealmRole: realm={}, oldRole={}, newRole={}", 
                roleRequest.getRealmName(), roleRequest.getName(), roleRequest.getNewName());
        return ResponseEntity.ok(roleService.updateRealmRole(roleRequest));
    }

    @DeleteMapping(value = "/realm/delete")
    public ResponseEntity<CommonResponseDTO> deleteRealmRole(
            @RequestParam(value = "realmName") String realmName,
            @RequestParam(value = "roleName") String roleName) {
        log.info("REST Request - deleteRealmRole: realm={}, role={}", realmName, roleName);
        return ResponseEntity.ok(roleService.deleteRealmRole(realmName, roleName));
    }
}
