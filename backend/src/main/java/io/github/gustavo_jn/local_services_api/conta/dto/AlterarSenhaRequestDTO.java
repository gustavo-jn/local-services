package io.github.gustavo_jn.local_services_api.conta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.github.gustavo_jn.local_services_api.conta.validation.SenhaDentroDoLimite;

public record AlterarSenhaRequestDTO(
		@NotBlank(message = "Informe a senha atual") @SenhaDentroDoLimite String senhaAtual,
		@NotBlank(message = "Informe a nova senha")
		@Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres") @SenhaDentroDoLimite String novaSenha) {
	@Override public String toString() { return "AlterarSenhaRequestDTO[conteúdo omitido]"; }
}
