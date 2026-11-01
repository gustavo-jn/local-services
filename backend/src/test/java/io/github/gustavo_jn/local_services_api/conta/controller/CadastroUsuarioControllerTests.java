package io.github.gustavo_jn.local_services_api.conta.controller;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import io.github.gustavo_jn.local_services_api.conta.repository.UsuarioRepository;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CadastroUsuarioControllerTests {

	private static final String REGISTER = "/api/v1/auth/register";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	private UsuarioRepository repository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void cadastraComTokenDaSessaoNormalizaEmailEArmazenaBcrypt() throws Exception {
		String email = emailUnico();
		String senha = " senha de teste 123! ";
		var csrfResult = mvc.perform(get("/api/v1/auth/csrf"))
				.andExpect(status().isOk()).andReturn();
		var csrfBody = jsonMapper.readTree(csrfResult.getResponse().getContentAsString());
		var session = (MockHttpSession) csrfResult.getRequest().getSession(false);
		assertNotNull(session);

		var result = mvc.perform(post(REGISTER).session(session)
				.header(csrfBody.get("headerName").asText(), csrfBody.get("token").asText())
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload("Usuario Teste", "  " + email.toUpperCase(java.util.Locale.ROOT) + "  ", senha)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.nome").value("Usuario Teste"))
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.senha").doesNotExist())
				.andExpect(jsonPath("$.senhaHash").doesNotExist()).andReturn();

		var usuario = repository.findByEmail(email).orElseThrow();
		assertEquals(usuario.getId().toString(), jsonMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
		String hash = jdbcTemplate.queryForObject("SELECT senha_hash FROM usuarios WHERE id = ?", String.class, usuario.getId());
		assertNotEquals(senha, hash);
		assertTrue(passwordEncoder.matches(senha, hash));
		assertFalse(passwordEncoder.matches(senha.trim(), hash));
	}

	@Test
	void rejeitaEmailDuplicadoCom409SemSegundaConta() throws Exception {
		String email = emailUnico();
		mvc.perform(post(REGISTER).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(payload("Usuario Teste", email, "Senha123!"))).andExpect(status().isCreated());
		mvc.perform(post(REGISTER).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(payload("Outra Conta", " " + email.toUpperCase(java.util.Locale.ROOT) + " ", "Senha456!")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"))
				.andExpect(jsonPath("$.fieldErrors.email").exists());
		assertEquals(1L, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios WHERE email = ?", Long.class, email));
	}

	@Test
	void rejeitaDadosInvalidosCom400SemPersistir() throws Exception {
		long quantidadeAntes = repository.count();
		mvc.perform(post(REGISTER).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(payload(" ", "email-invalido", "curta")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.fieldErrors.nome").exists())
				.andExpect(jsonPath("$.fieldErrors.email").exists())
				.andExpect(jsonPath("$.fieldErrors.senha").exists())
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.path").value(REGISTER));
		assertEquals(quantidadeAntes, repository.count());
	}

	@Test
	void rejeitaSenhaMaiorQue72BytesSemPersistir() throws Exception {
		String email = emailUnico();
		mvc.perform(post(REGISTER).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(payload("Usuario Teste", email, "á".repeat(37))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.senha").exists());
		assertFalse(repository.existsByEmail(email));
	}

	@Test
	void rejeitaJsonMalformadoCom400() throws Exception {
		mvc.perform(post(REGISTER).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
	}

	@Test
	void rejeitaCorpoAusenteCom400() throws Exception {
		mvc.perform(post(REGISTER).with(csrf()).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
	}

	@Test
	void rejeitaCadastroSemCsrfCom403SemPersistir() throws Exception {
		String email = emailUnico();
		mvc.perform(post(REGISTER).contentType(MediaType.APPLICATION_JSON)
				.content(payload("Usuario Teste", email, "Senha123!")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("INVALID_CSRF_TOKEN"));
		assertFalse(repository.existsByEmail(email));
	}

	@Test
	void rejeitaCadastroComCsrfInvalidoCom403SemPersistir() throws Exception {
		String email = emailUnico();
		mvc.perform(post(REGISTER).with(csrf().useInvalidToken()).contentType(MediaType.APPLICATION_JSON)
				.content(payload("Usuario Teste", email, "Senha123!")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("INVALID_CSRF_TOKEN"));
		assertFalse(repository.existsByEmail(email));
	}

	private String payload(String nome, String email, String senha) {
		return jsonMapper.writeValueAsString(Map.of("nome", nome, "email", email, "senha", senha));
	}

	private String emailUnico() {
		return "teste-" + UUID.randomUUID() + "@example.com";
	}

}
