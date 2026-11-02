package io.github.gustavo_jn.local_services_api.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.gustavo_jn.local_services_api.config.JwtCookieBearerTokenResolver;

@RestController
public class LogoutController {
	private final boolean secure;
	public LogoutController(@Value("${app.security.jwt.cookie-secure}") boolean secure) { this.secure = secure; }

	@PostMapping("/api/v1/auth/logout")
	public ResponseEntity<Void> sair(HttpServletRequest request) {
		var session = request.getSession(false);
		if (session != null) session.invalidate();
		return ResponseEntity.noContent().cacheControl(CacheControl.noStore())
				.header(HttpHeaders.SET_COOKIE, apagar(JwtCookieBearerTokenResolver.COOKIE_NAME, "/api"), apagar("JSESSIONID", "/"))
				.build();
	}
	private String apagar(String nome, String path) {
		return ResponseCookie.from(nome, "").httpOnly(true).secure(secure).sameSite("Lax")
				.path(path).maxAge(0).build().toString();
	}
}
