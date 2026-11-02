package io.github.gustavo_jn.local_services_api.perfil.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record PerfilProfissionalRequestDTO(
		@NotBlank(message = "Informe o nome profissional") @Size(min = 2, max = 120, message = "Use entre 2 e 120 caracteres") String nome,
		@NotBlank(message = "Informe a descrição") @Size(min = 10, max = 3000, message = "Use entre 10 e 3000 caracteres") String descricao,
		Boolean aceitaNovosServicos) {
	public PerfilProfissionalRequestDTO {
		nome = nome == null ? null : nome.trim();
		descricao = descricao == null ? null : descricao.trim();
	}
}
