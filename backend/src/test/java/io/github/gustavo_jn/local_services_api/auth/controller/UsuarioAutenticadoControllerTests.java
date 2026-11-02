package io.github.gustavo_jn.local_services_api.auth.controller;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import io.github.gustavo_jn.local_services_api.auth.service.UsuarioAutenticadoService;
import io.github.gustavo_jn.local_services_api.config.ApiAccessDeniedHandler;
import io.github.gustavo_jn.local_services_api.config.ApiAuthenticationEntryPoint;
import io.github.gustavo_jn.local_services_api.config.JwtConfiguration;
import io.github.gustavo_jn.local_services_api.config.JwtCookieBearerTokenResolver;
import io.github.gustavo_jn.local_services_api.config.SecurityConfig;
import io.github.gustavo_jn.local_services_api.conta.entity.Usuario;
import io.github.gustavo_jn.local_services_api.conta.repository.UsuarioRepository;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UsuarioAutenticadoController.class, properties = {
		// Chave fictícia, utilizada somente pelo contexto isolado deste teste.
		"app.security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
		"app.security.jwt.issuer=local-services-api",
		"app.security.jwt.audience=local-services-api"
})
@Import({ SecurityConfig.class, JwtConfiguration.class, JwtCookieBearerTokenResolver.class,
		ApiAuthenticationEntryPoint.class, ApiAccessDeniedHandler.class, UsuarioAutenticadoService.class,
		UsuarioAutenticadoControllerTests.SecurityTestConfiguration.class })
class UsuarioAutenticadoControllerTests {

	@TestConfiguration
	@EnableWebSecurity
	static class SecurityTestConfiguration {
	}

	private static final String ME = "/api/v1/auth/me";
	private static final String ISSUER = "local-services-api";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JwtEncoder jwtEncoder;

	@MockitoBean
	private UsuarioRepository repository;

	@Test
	void retornaDadosAtuaisDaContaIdentificadaPeloCookieSemCsrf() throws Exception {
		UUID id = UUID.randomUUID();
		Usuario usuario = new Usuario("Nome atualizado", "atualizado@example.com", "hash-nao-publico");
		ReflectionTestUtils.setField(usuario, "id", id);
		when(repository.findById(id)).thenReturn(Optional.of(usuario));

		mvc.perform(get(ME).cookie(cookie(token(id.toString())))
				.param("id", UUID.randomUUID().toString()).param("email", "outra@example.com"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
				.andExpect(jsonPath("$", aMapWithSize(3)))
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.nome").value("Nome atualizado"))
				.andExpect(jsonPath("$.email").value("atualizado@example.com"))
				.andExpect(jsonPath("$.senha").doesNotExist())
				.andExpect(jsonPath("$.senhaHash").doesNotExist())
				.andExpect(jsonPath("$.token").doesNotExist());
		verify(repository).findById(id);
		verifyNoMoreInteractions(repository);
	}

	@Test
	void rejeitaRequisicaoSemCookieJwt() throws Exception {
		esperarNaoAutorizado(mvc.perform(get(ME)));
		verifyNoInteractions(repository);
	}

	@Test
	void sessaoCsrfSozinhaNaoAutentica() throws Exception {
		esperarNaoAutorizado(mvc.perform(get(ME).cookie(new Cookie("JSESSIONID", "sessao-teste"))));
		verifyNoInteractions(repository);
	}

	@Test
	void naoAceitaJwtNoCabecalhoAuthorization() throws Exception {
		esperarNaoAutorizado(mvc.perform(get(ME)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token(UUID.randomUUID().toString()))));
		verifyNoInteractions(repository);
	}

	@Test
	void rejeitaTokenAssinadoComOutraChave() throws Exception {
		byte[] outraChave = Base64.getDecoder().decode("ZmVkY2JhOTg3NjU0MzIxMGZlZGNiYTk4NzY1NDMyMTA=");
		JwtEncoder outroEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(outraChave, "HmacSHA256")));
		String token = assinar(outroEncoder, claims(UUID.randomUUID().toString(), ISSUER,
				List.of(ISSUER), Instant.now().plusSeconds(300)));
		esperarNaoAutorizado(mvc.perform(get(ME).cookie(cookie(token))));
		verifyNoInteractions(repository);
	}

	@Test
	void rejeitaTokenExpirado() throws Exception {
		String token = assinar(jwtEncoder, claims(UUID.randomUUID().toString(), ISSUER,
				List.of(ISSUER), Instant.now().minusSeconds(120)));
		esperarNaoAutorizado(mvc.perform(get(ME).cookie(cookie(token))));
		verifyNoInteractions(repository);
	}

	@Test
	void rejeitaEmissorIncorreto() throws Exception {
		String token = assinar(jwtEncoder, claims(UUID.randomUUID().toString(), "outro-emissor",
				List.of(ISSUER), Instant.now().plusSeconds(300)));
		esperarNaoAutorizado(mvc.perform(get(ME).cookie(cookie(token))));
		verifyNoInteractions(repository);
	}

	@Test
	void rejeitaAudienciaIncorreta() throws Exception {
		String token = assinar(jwtEncoder, claims(UUID.randomUUID().toString(), ISSUER,
				List.of("outra-api"), Instant.now().plusSeconds(300)));
		esperarNaoAutorizado(mvc.perform(get(ME).cookie(cookie(token))));
		verifyNoInteractions(repository);
	}

	@Test
	void rejeitaTokenSemAudiencia() throws Exception {
		JwtClaimsSet claims = JwtClaimsSet.builder().issuer(ISSUER).subject(UUID.randomUUID().toString())
				.issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300)).build();
		esperarNaoAutorizado(mvc.perform(get(ME).cookie(cookie(assinar(jwtEncoder, claims)))));
		verifyNoInteractions(repository);
	}

	@ParameterizedTest
	@ValueSource(strings = { "nao-e-um-jwt", "", "   " })
	void rejeitaCookieMalformadoOuVazio(String token) throws Exception {
		esperarNaoAutorizado(mvc.perform(get(ME).cookie(cookie(token))));
		verifyNoInteractions(repository);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "id-invalido", "   " })
	void rejeitaSubjectAusenteOuInvalidoSemConsultarBanco(String subject) throws Exception {
		esperarNaoAutorizado(mvc.perform(get(ME).cookie(cookie(token(subject)))));
		verifyNoInteractions(repository);
	}

	@Test
	void rejeitaContaQueNaoExisteMais() throws Exception {
		UUID id = UUID.randomUUID();
		when(repository.findById(id)).thenReturn(Optional.empty());
		esperarNaoAutorizado(mvc.perform(get(ME).cookie(cookie(token(id.toString())))));
		verify(repository).findById(id);
	}

	private String token(String subject) {
		return assinar(jwtEncoder, claims(subject, ISSUER, List.of(ISSUER), Instant.now().plusSeconds(300)));
	}

	private JwtClaimsSet claims(String subject, String issuer, List<String> audience, Instant expiration) {
		JwtClaimsSet.Builder builder = JwtClaimsSet.builder().issuer(issuer).audience(audience)
				.issuedAt(Instant.now().minusSeconds(300)).expiresAt(expiration);
		if (subject != null) {
			builder.subject(subject);
		}
		return builder.build();
	}

	private String assinar(JwtEncoder encoder, JwtClaimsSet claims) {
		return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
				.getTokenValue();
	}

	private Cookie cookie(String token) {
		return new Cookie(JwtCookieBearerTokenResolver.COOKIE_NAME, token);
	}

	private void esperarNaoAutorizado(ResultActions result) throws Exception {
		result.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
				.andExpect(jsonPath("$.message").value("Autenticação necessária"))
				.andExpect(jsonPath("$.path").value(ME))
				.andExpect(jsonPath("$.timestamp").exists());
	}
}
