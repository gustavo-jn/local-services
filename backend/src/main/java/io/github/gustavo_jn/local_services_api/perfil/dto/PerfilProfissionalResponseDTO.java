package io.github.gustavo_jn.local_services_api.perfil.dto;
import java.util.UUID;
public record PerfilProfissionalResponseDTO(UUID id, String nome, String descricao,
		boolean aceitaNovosServicos, boolean publicado, AgendaDTO agenda) {
	public record AgendaDTO(UUID id, String fusoReferencia) { }
}
