package io.github.gustavo_jn.local_services_api.conta.controller;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@Import(CadastroUsuarioConcorrenciaTests.ConcurrencyConfig.class)
class CadastroUsuarioConcorrenciaTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ConcurrentPasswordEncoder concurrentEncoder;

	@Test
	void cadastrosSimultaneosCriamUmaContaERetornam201E409() throws Exception {
		String email = "concorrencia-" + UUID.randomUUID() + "@example.com";
		String senha = "Senha de teste 123!";
		String primeiroPayload = payload(email, senha);
		String segundoPayload = payload("  " + email.toUpperCase(Locale.ROOT) + "  ", senha);
		var executor = Executors.newFixedThreadPool(2);
		try {
			var primeira = executor.submit(() -> mvc.perform(post("/api/v1/auth/register").with(csrf())
					.contentType(MediaType.APPLICATION_JSON).content(primeiroPayload)).andReturn());
			var segunda = executor.submit(() -> mvc.perform(post("/api/v1/auth/register").with(csrf())
					.contentType(MediaType.APPLICATION_JSON).content(segundoPayload)).andReturn());

			var resultados = List.of(primeira.get(20, TimeUnit.SECONDS), segunda.get(20, TimeUnit.SECONDS));
			assertEquals(List.of(201, 409), resultados.stream()
					.map(result -> result.getResponse().getStatus()).sorted().toList());
			assertEquals(2, concurrentEncoder.transactionIds.size(), "As requisições devem usar transações diferentes");
			assertEquals(1L, jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM usuarios WHERE email = ?", Long.class, email));

			for (var result : resultados) {
				var body = jsonMapper.readTree(result.getResponse().getContentAsString());
				assertFalse(body.has("senha"));
				assertFalse(body.has("senhaHash"));
				if (result.getResponse().getStatus() == 201) {
					assertEquals(email, body.get("email").asText());
					assertEquals(body.get("id").asText(), jdbcTemplate.queryForObject(
							"SELECT id::text FROM usuarios WHERE email = ?", String.class, email));
				} else {
					assertEquals("EMAIL_ALREADY_REGISTERED", body.get("code").asText());
					assertTrue(body.get("fieldErrors").has("email"));
				}
			}
		} finally {
			executor.shutdownNow();
			if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				throw new IllegalStateException("As requisições de teste não encerraram; verificar e-mail " + email);
			}
			jdbcTemplate.update("DELETE FROM usuarios WHERE email = ?", email);
		}
	}

	private String payload(String email, String senha) {
		return jsonMapper.writeValueAsString(Map.of("nome", "Teste Concorrente", "email", email, "senha", senha));
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class ConcurrencyConfig {

		@Bean
		@Primary
		ConcurrentPasswordEncoder concurrentPasswordEncoder(
				@Qualifier("passwordEncoder") PasswordEncoder delegate, JdbcTemplate jdbcTemplate) {
			return new ConcurrentPasswordEncoder(delegate, jdbcTemplate);
		}
	}

	// Sincroniza apenas o teste, após ambas as consultas de e-mail. O repositório,
	// as transações, a restrição de unicidade e o bcrypt continuam sendo reais.
	static class ConcurrentPasswordEncoder implements PasswordEncoder {

		private final PasswordEncoder delegate;
		private final JdbcTemplate jdbcTemplate;
		private final CountDownLatch ready = new CountDownLatch(2);
		private final Set<Long> transactionIds = ConcurrentHashMap.newKeySet();

		ConcurrentPasswordEncoder(PasswordEncoder delegate, JdbcTemplate jdbcTemplate) {
			this.delegate = delegate;
			this.jdbcTemplate = jdbcTemplate;
		}

		@Override
		public String encode(CharSequence rawPassword) {
			if (!TransactionSynchronizationManager.isActualTransactionActive()) {
				throw new IllegalStateException("O cadastro deve executar em uma transação real");
			}
			transactionIds.add(jdbcTemplate.queryForObject("SELECT txid_current()", Long.class));
			ready.countDown();
			try {
				if (!ready.await(10, TimeUnit.SECONDS)) {
					throw new IllegalStateException("As duas requisições não alcançaram o cadastro simultaneamente");
				}
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Teste de concorrência interrompido", exception);
			}
			return delegate.encode(rawPassword);
		}

		@Override
		public boolean matches(CharSequence rawPassword, String encodedPassword) {
			return delegate.matches(rawPassword, encodedPassword);
		}
	}

}
