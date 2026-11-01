package io.github.gustavo_jn.local_services_api.conta;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SenhaDentroDoLimiteValidatorTests {

	@Test
	void aceitaSenhasComAte72Bytes() {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			for (String senha : new String[] {"a".repeat(71), "a".repeat(72), "á".repeat(36), "😀".repeat(18)}) {
				assertTrue(factory.getValidator().validate(cadastro(senha)).isEmpty());
			}
		}
	}

	@Test
	void rejeitaSenhasAcimaDe72BytesInclusiveComCaracteresMultibyte() {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			for (String senha : new String[] {"a".repeat(73), "á".repeat(36) + "a", "😀".repeat(18) + "a"}) {
				assertTrue(factory.getValidator().validate(cadastro(senha)).stream()
						.anyMatch(violation -> violation.getPropertyPath().toString().equals("senha")
								&& violation.getConstraintDescriptor().getAnnotation() instanceof SenhaDentroDoLimite));
			}
		}
	}

	@Test
	void senhaAusenteOuCurtaContinuaInvalida() {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			assertFalse(factory.getValidator().validate(cadastro(null)).isEmpty());
			assertFalse(factory.getValidator().validate(cadastro("")).isEmpty());
			assertFalse(factory.getValidator().validate(cadastro("1234567")).isEmpty());
		}
	}

	private CadastroUsuarioRequestDTO cadastro(String senha) {
		return new CadastroUsuarioRequestDTO("Usuario Teste", "usuario@example.com", senha);
	}

}
