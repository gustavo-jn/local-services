package io.github.gustavo_jn.local_services_api.conta.dto;

import java.util.UUID;

public record UsuarioResponseDTO(UUID id, String nome, String email) {
}
