package io.github.gustavo_jn.local_services_api.auth.controller;

import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.github.gustavo_jn.local_services_api.auth.dto.LoginRequestDTO;
import io.github.gustavo_jn.local_services_api.auth.service.LoginService;
import io.github.gustavo_jn.local_services_api.config.JwtCookieBearerTokenResolver;
import io.github.gustavo_jn.local_services_api.conta.dto.UsuarioResponseDTO;

@RestController
public class LoginController {

	private final LoginService loginService;
	private final CsrfTokenRepository csrfTokenRepository;
	private final Duration expiration;
	private final boolean cookieSecure;

	public LoginController(
			LoginService loginService,
			CsrfTokenRepository csrfTokenRepository,
			@Value("${app.security.jwt.expiration}") Duration expiration,
			@Value("${app.security.jwt.cookie-secure}") boolean cookieSecure) {
		this.loginService = loginService;
		this.csrfTokenRepository = csrfTokenRepository;
		this.expiration = expiration;
		this.cookieSecure = cookieSecure;
	}

	@PostMapping("/api/v1/auth/login")
	public ResponseEntity<UsuarioResponseDTO> login(
			@Valid @RequestBody LoginRequestDTO request,
			HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		LoginService.ResultadoLogin resultado = loginService.autenticar(request);

		if (servletRequest.getSession(false) != null) {
			servletRequest.changeSessionId();
		}
		csrfTokenRepository.saveToken(null, servletRequest, servletResponse);

		ResponseCookie accessCookie = ResponseCookie.from(JwtCookieBearerTokenResolver.COOKIE_NAME, resultado.token())
				.httpOnly(true)
				.secure(cookieSecure)
				.sameSite("Lax")
				.path("/api")
				.maxAge(expiration)
				.build();

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, accessCookie.toString())
				.cacheControl(CacheControl.noStore())
				.body(resultado.usuario());
	}
}
