package io.github.gustavo_jn.local_services_api.conta.dto;

import java.util.Locale;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import io.github.gustavo_jn.local_services_api.conta.validation.SenhaDentroDoLimite;

public record AlterarContaRequestDTO(
		@Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres") String nome,
		@Email(message = "Informe um e-mail válido")
		@Size(min = 3, max = 254, message = "O e-mail deve ter até 254 caracteres") String email,
		@SenhaDentroDoLimite String senhaAtual) {
	public AlterarContaRequestDTO {
		nome = nome == null ? null : nome.trim();
		email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}
	@Override public String toString() { return "AlterarContaRequestDTO[conteúdo omitido]"; }
}
