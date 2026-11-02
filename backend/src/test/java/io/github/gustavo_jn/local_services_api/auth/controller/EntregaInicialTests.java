package io.github.gustavo_jn.local_services_api.auth.controller;

import java.util.Map;
import java.util.UUID;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import io.github.gustavo_jn.local_services_api.conta.entity.Usuario;
import io.github.gustavo_jn.local_services_api.conta.repository.UsuarioRepository;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @Transactional
class EntregaInicialTests {
	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarios;
	@Autowired PasswordEncoder encoder;
	@Autowired JdbcTemplate jdbc;
	@Autowired JsonMapper json;
	private static final String SENHA = " senha teste 123! ";
	private static final String PERFIL = "/api/v1/me/perfil-profissional";

	@Test void loginEmiteCookieSeguroERetornaContaSemSegredos() throws Exception {
		var usuario = usuario();
		var login = entrar(usuario.getEmail(), SENHA);
		String cookie = login.getResponse().getHeader("Set-Cookie");
		assertNotNull(cookie);
		assertTrue(cookie.contains("HttpOnly")); assertTrue(cookie.contains("Path=/api"));
		assertTrue(cookie.contains("SameSite=Lax")); assertTrue(cookie.contains("Max-Age=1800"));
		mvc.perform(get("/api/v1/me").cookie(cookie(login)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(usuario.getId().toString()))
				.andExpect(jsonPath("$.senhaHash").doesNotExist()).andExpect(jsonPath("$.senha").doesNotExist());
	}
	@Test void credenciaisInvalidasNaoEmitemCookie() throws Exception {
		var usuario = usuario();
		for (String email : new String[]{usuario.getEmail(), "inexistente-"+UUID.randomUUID()+"@example.com"}) {
			mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
					.content(body(Map.of("email", email, "senha", "SenhaIncorreta123!"))))
					.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
					.andExpect(header().doesNotExist("Set-Cookie"));
		}
	}
	@Test void renovaCsrfDepoisDoLoginEApagaCookiesNoLogout() throws Exception {
		var usuario = usuario();
		var inicio = mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
		var sessao = (MockHttpSession) inicio.getRequest().getSession(false);
		assertNotNull(sessao);
		String idAnterior = sessao.getId();
		String tokenAnterior = json.readTree(inicio.getResponse().getContentAsString()).get("token").asText();
		var login = mvc.perform(post("/api/v1/auth/login").session(sessao).header("X-CSRF-TOKEN", tokenAnterior)
				.contentType(MediaType.APPLICATION_JSON).content(body(Map.of("email", usuario.getEmail(), "senha", SENHA))))
				.andExpect(status().isOk()).andReturn();
		assertNotEquals(idAnterior, sessao.getId());
		mvc.perform(patch("/api/v1/me").cookie(cookie(login)).session(sessao).header("X-CSRF-TOKEN", tokenAnterior)
				.contentType(MediaType.APPLICATION_JSON).content(body(Map.of("nome", "Outro nome"))))
				.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("INVALID_CSRF_TOKEN"));
		var novo = mvc.perform(get("/api/v1/auth/csrf").session(sessao)).andReturn();
		String novoToken = json.readTree(novo.getResponse().getContentAsString()).get("token").asText();
		var logout = mvc.perform(post("/api/v1/auth/logout").session(sessao).cookie(cookie(login))
				.header("X-CSRF-TOKEN", novoToken)).andExpect(status().isNoContent()).andReturn();
		assertTrue(logout.getResponse().getHeaders("Set-Cookie").stream().anyMatch(valor -> valor.startsWith("LS_ACCESS_TOKEN=") && valor.contains("Max-Age=0") && valor.contains("Path=/api")));
		assertTrue(sessao.isInvalid());
		mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
		// Contrato stateless: apagar o cookie não revoga uma cópia de JWT ainda válida.
		mvc.perform(get("/api/v1/auth/me").cookie(cookie(login))).andExpect(status().isOk());
	}
	@Test void logoutExigeCsrfEAceitaCookieJwtExpiradoOuInvalido() throws Exception {
		mvc.perform(post("/api/v1/auth/logout")).andExpect(status().isForbidden());
		mvc.perform(post("/api/v1/auth/logout").with(csrf()).cookie(new Cookie("LS_ACCESS_TOKEN", "invalido")))
				.andExpect(status().isNoContent());
	}
	@Test void alteraSomenteAContaDoJwtEConfirmaSenhaParaEmail() throws Exception {
		var usuario = usuario(); var outra = usuario(); var cookie = cookie(entrar(usuario.getEmail(), SENHA));
		mvc.perform(patch("/api/v1/me").with(csrf()).cookie(cookie).contentType(MediaType.APPLICATION_JSON)
				.content(body(Map.of("nome", "Nome atualizado", "id", outra.getId().toString()))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(usuario.getId().toString()))
				.andExpect(jsonPath("$.nome").value("Nome atualizado"));
		assertEquals("Usuario Teste", usuarios.findById(outra.getId()).orElseThrow().getNome());
		String novoEmail = "alterado-"+UUID.randomUUID()+"@example.com";
		mvc.perform(patch("/api/v1/me").with(csrf()).cookie(cookie).contentType(MediaType.APPLICATION_JSON)
				.content(body(Map.of("email", novoEmail, "senhaAtual", "errada"))))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_CURRENT_PASSWORD"));
		String anterior = usuario.getEmail();
		mvc.perform(patch("/api/v1/me").with(csrf()).cookie(cookie).contentType(MediaType.APPLICATION_JSON)
				.content(body(Map.of("email", " "+novoEmail.toUpperCase(java.util.Locale.ROOT)+" ", "senhaAtual", SENHA))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.email").value(novoEmail));
		assertFalse(usuarios.existsByEmail(anterior));
		entrar(novoEmail, SENHA);
	}
	@Test void rejeitaEmailDuplicadoEDadosInvalidosSemModificarConta() throws Exception {
		var usuario = usuario(); var outra = usuario(); var cookie = cookie(entrar(usuario.getEmail(), SENHA));
		mvc.perform(patch("/api/v1/me").with(csrf()).cookie(cookie).contentType(MediaType.APPLICATION_JSON)
				.content(body(Map.of("email", outra.getEmail(), "senhaAtual", SENHA))))
				.andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
		mvc.perform(patch("/api/v1/me").with(csrf()).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\" \"}"))
				.andExpect(status().isBadRequest());
		mvc.perform(patch("/api/v1/me").with(csrf()).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
		assertEquals("Usuario Teste", usuarios.findById(usuario.getId()).orElseThrow().getNome());
	}
	@Test void trocaSenhaERejeitaSenhaAnteriorNoLogin() throws Exception {
		var usuario = usuario(); var cookie = cookie(entrar(usuario.getEmail(), SENHA));
		for (Map<String, String> dados : java.util.List.of(Map.of("senhaAtual", "errada", "novaSenha", "NovaSenha123!"), Map.of("senhaAtual", SENHA, "novaSenha", "curta"), Map.of("senhaAtual", SENHA, "novaSenha", "á".repeat(37)))) {
			mvc.perform(patch("/api/v1/me/senha").with(csrf()).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(body(dados)))
					.andExpect(status().isBadRequest());
		}
		mvc.perform(patch("/api/v1/me/senha").with(csrf()).cookie(cookie).contentType(MediaType.APPLICATION_JSON)
				.content(body(Map.of("senhaAtual", SENHA, "novaSenha", " NovaSenha123! ")))).andExpect(status().isNoContent());
		mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(body(Map.of("email", usuario.getEmail(), "senha", SENHA)))).andExpect(status().isUnauthorized());
		entrar(usuario.getEmail(), " NovaSenha123! ");
	}
	@Test void criaPerfilEAgendaJuntosEEditaDadosSemPublicar() throws Exception {
		var usuario = usuario(); var cookie = cookie(entrar(usuario.getEmail(), SENHA));
		mvc.perform(get(PERFIL).cookie(cookie)).andExpect(status().isNotFound());
		var result = mvc.perform(post(PERFIL).cookie(cookie).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(perfil("Nome profissional")))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.publicado").value(false))
				.andExpect(jsonPath("$.aceitaNovosServicos").value(true)).andExpect(jsonPath("$.agenda.fusoReferencia").value("America/Sao_Paulo")).andReturn();
		String id = json.readTree(result.getResponse().getContentAsString()).get("id").asText();
		mvc.perform(patch(PERFIL).cookie(cookie).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(body(Map.of("nome", "Nome editado", "descricao", "Descrição atualizada do trabalho.", "aceitaNovosServicos", false, "publicado", true))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id)).andExpect(jsonPath("$.publicado").value(false))
				.andExpect(jsonPath("$.aceitaNovosServicos").value(false));
		mvc.perform(post(PERFIL).cookie(cookie).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(perfil("Outro perfil")))
				.andExpect(status().isConflict());
		assertEquals(1L, jdbc.queryForObject("select count(*) from agendas where perfil_profissional_id = ?", Long.class, UUID.fromString(id)));
	}
	@Test void outraContaNaoConsultaNemAlteraPerfilAlheio() throws Exception {
		var primeiro = usuario(); var segundo = usuario();
		var primeiroCookie = cookie(entrar(primeiro.getEmail(), SENHA)); var segundoCookie = cookie(entrar(segundo.getEmail(), SENHA));
		mvc.perform(post(PERFIL).with(csrf()).cookie(primeiroCookie).contentType(MediaType.APPLICATION_JSON).content(perfil("Primeiro"))).andExpect(status().isCreated());
		mvc.perform(get(PERFIL).cookie(segundoCookie).param("usuarioId", primeiro.getId().toString())).andExpect(status().isNotFound());
		mvc.perform(patch(PERFIL).with(csrf()).cookie(segundoCookie).contentType(MediaType.APPLICATION_JSON).content(perfil("Intruso"))).andExpect(status().isNotFound());
		mvc.perform(get(PERFIL).cookie(primeiroCookie)).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Primeiro"));
	}
	@Test void validaPerfilAntesDeGravarEProtegeOperacoesPrivadas() throws Exception {
		var usuario = usuario(); var cookie = cookie(entrar(usuario.getEmail(), SENHA));
		mvc.perform(post(PERFIL).with(csrf()).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"x\",\"descricao\":\"curta\"}"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.nome").exists()).andExpect(jsonPath("$.fieldErrors.descricao").exists());
		assertEquals(0L, jdbc.queryForObject("select count(*) from perfis_profissionais where usuario_id = ?", Long.class, usuario.getId()));
		mvc.perform(post(PERFIL).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(perfil("Teste"))).andExpect(status().isForbidden());
		mvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized());
		mvc.perform(patch("/api/v1/me").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Teste\"}")).andExpect(status().isUnauthorized());
	}
	private Usuario usuario() { return usuarios.saveAndFlush(new Usuario("Usuario Teste", "entrega-"+UUID.randomUUID()+"@example.com", encoder.encode(SENHA))); }
	private MvcResult entrar(String email, String senha) throws Exception {
		return mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(body(Map.of("email", email, "senha", senha)))).andExpect(status().isOk()).andReturn();
	}
	private Cookie cookie(MvcResult result) {
		String header = result.getResponse().getHeader("Set-Cookie"); assertNotNull(header);
		return new Cookie("LS_ACCESS_TOKEN", header.split(";",2)[0].split("=",2)[1]);
	}
	private String body(Object payload) { return json.writeValueAsString(payload); }
	private String perfil(String nome) { return body(Map.of("nome", nome, "descricao", "Descrição do trabalho oferecido.")); }
}
