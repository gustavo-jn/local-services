package io.github.gustavo_jn.local_services_api.auth.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.gustavo_jn.local_services_api.auth.service.UsuarioAutenticadoService;
import io.github.gustavo_jn.local_services_api.conta.dto.UsuarioResponseDTO;

@RestController
public class UsuarioAutenticadoController {

	private final UsuarioAutenticadoService usuarioAutenticadoService;

	public UsuarioAutenticadoController(UsuarioAutenticadoService usuarioAutenticadoService) {
		this.usuarioAutenticadoService = usuarioAutenticadoService;
	}

	@GetMapping("/api/v1/auth/me")
	public ResponseEntity<UsuarioResponseDTO> obter(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok()
				.cacheControl(CacheControl.noStore())
				.body(usuarioAutenticadoService.obter(jwt.getSubject()));
	}
}
