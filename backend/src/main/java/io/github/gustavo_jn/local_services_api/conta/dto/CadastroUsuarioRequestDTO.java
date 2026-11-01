package io.github.gustavo_jn.local_services_api.conta.dto;

import io.github.gustavo_jn.local_services_api.conta.validation.SenhaDentroDoLimite;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroUsuarioRequestDTO(
		@NotBlank(message = "Informe o nome")
		@Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres")
		String nome,

		@NotBlank(message = "Informe o e-mail")
		@Email(message = "Informe um e-mail válido")
		@Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres")
		String email,

		@NotBlank(message = "Informe a senha")
		@Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
		@SenhaDentroDoLimite
		String senha) {

	@Override
	public String toString() {
		return "CadastroUsuarioRequestDTO[conteúdo omitido]";
	}

}
