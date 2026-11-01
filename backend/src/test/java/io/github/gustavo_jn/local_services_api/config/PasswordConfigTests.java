package io.github.gustavo_jn.local_services_api.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordConfigTests {

	private final PasswordEncoder passwordEncoder = new PasswordConfig().passwordEncoder();

	@Test
	void hashAceitaSenhaCorretaERejeitaSenhaIncorreta() {
		String senha = "Senha de teste 123!";
		String hash = passwordEncoder.encode(senha);

		assertNotEquals(senha, hash);
		assertEquals(60, hash.length());
		assertTrue(hash.startsWith("$2a$12$"));
		assertTrue(passwordEncoder.matches(senha, hash));
		assertFalse(passwordEncoder.matches("Outra senha 123!", hash));
	}

	@Test
	void mesmaSenhaGeraHashesDiferentesEValidos() {
		String senha = "Senha de teste 123!";
		String primeiroHash = passwordEncoder.encode(senha);
		String segundoHash = passwordEncoder.encode(senha);

		assertNotEquals(primeiroHash, segundoHash);
		assertTrue(passwordEncoder.matches(senha, primeiroHash));
		assertTrue(passwordEncoder.matches(senha, segundoHash));
	}

}
