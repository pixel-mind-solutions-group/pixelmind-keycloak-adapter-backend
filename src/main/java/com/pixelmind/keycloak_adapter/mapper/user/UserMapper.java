package com.pixelmind.keycloak_adapter.mapper.user;

import com.pixelmind.keycloak_adapter.dto.user.UserRequestDTO;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserMapper {

    public UserRepresentation toUserRepresentation(UserRepresentation user, UserRequestDTO userRequest) {

        // ── Step 1: Build user representation ────────────────────
        user.setEmail(userRequest.getEmail());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEnabled(userRequest.isEnabled());
        user.setEmailVerified(userRequest.isEmailVerified());

        if (userRequest.getUserRole() != null && !userRequest.getUserRole().trim().isEmpty()) {
            Map<String, List<String>> attributes = user.getAttributes();
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            attributes.put("role_scope", List.of(userRequest.getUserRole()));
            user.setAttributes(attributes);
        }

        return user;
    }
}
