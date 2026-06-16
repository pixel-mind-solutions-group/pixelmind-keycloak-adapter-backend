package com.pixelmind.keycloak_adapter.mapper.realm;

import com.pixelmind.keycloak_adapter.dto.realm.RealmResponseDTO;
import com.pixelmind.keycloak_adapter.mapper.client.ClientMapper;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class RealmMapper {

    private final ClientMapper clientMapper;

    public RealmResponseDTO toRealmResponseDTO(RealmRepresentation realm) {
        if (realm == null) {
            return null;
        }
        RealmResponseDTO dto = new RealmResponseDTO();
        dto.setId(realm.getId());
        dto.setRealmName(realm.getRealm());
        dto.setDisplayName(realm.getDisplayName());
        dto.setEnabled(realm.isEnabled());
        dto.setClients(
                clientMapper.toClientResponseDTOs(
                        Optional.ofNullable(realm.getClients())
                                .orElse(Collections.emptyList())
                                .stream()
                                .filter(ClientRepresentation::isEnabled)
                                .collect(Collectors.toList())
                )
        );

        return dto;
    }

    public List<RealmResponseDTO> toRealmResponseDTOs(List<RealmRepresentation> realms) {

        return realms.stream()
                .map(this::toRealmResponseDTO)
                .toList();
    }
}
