package io.github.gustavo_jn.local_services_api.auth.dto;

import io.github.gustavo_jn.local_services_api.conta.validation.SenhaDentroDoLimite;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
		@NotBlank(message = "Informe o e-mail")
		@Email(message = "Informe um e-mail válido")
		@Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres")
		String email,

		@NotBlank(message = "Informe a senha")
		@SenhaDentroDoLimite
		String senha) {

	@Override
	public String toString() {
		return "LoginRequestDTO[email=" + email + ", senha=[REDACTED]]";
	}
}
