package com.pixelmind.keycloak_adapter.mapper.client;

import com.pixelmind.keycloak_adapter.dto.client.ClientResponseDTO;
import com.pixelmind.keycloak_adapter.dto.realm.RealmResponseDTO;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientMapper {

    public ClientResponseDTO toClientResponseDTO(ClientRepresentation client) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(client.getId());
        dto.setClientId(client.getClientId());
        dto.setName(client.getName());
        dto.setDescription(client.getDescription());
        dto.setActive(client.isEnabled());
        return dto;
    }

    public List<ClientResponseDTO> toClientResponseDTOs(List<ClientRepresentation> clients) {
        if (clients == null) {
            return List.of();
        }
        return clients.stream()
                .map(clientDto -> toClientResponseDTO(clientDto))
                .toList();
    }
}
