package io.github.gustavo_jn.local_services_api.conta.controller;

import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import io.github.gustavo_jn.local_services_api.auth.service.UsuarioAutenticadoService;
import io.github.gustavo_jn.local_services_api.conta.dto.*;
import io.github.gustavo_jn.local_services_api.conta.service.MinhaContaService;

@RestController
@RequestMapping("/api/v1/me")
public class MinhaContaController {
	private final UsuarioAutenticadoService identidade;
	private final MinhaContaService conta;
	public MinhaContaController(UsuarioAutenticadoService identidade, MinhaContaService conta) {
		this.identidade = identidade;
		this.conta = conta;
	}
	@GetMapping
	public ResponseEntity<UsuarioResponseDTO> obter(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(identidade.obter(jwt.getSubject()));
	}
	@PatchMapping
	public ResponseEntity<UsuarioResponseDTO> alterar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AlterarContaRequestDTO dados) {
		return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(conta.alterar(UsuarioAutenticadoService.identificar(jwt.getSubject()), dados));
	}
	@PatchMapping("/senha")
	public ResponseEntity<Void> alterarSenha(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AlterarSenhaRequestDTO dados) {
		conta.alterarSenha(UsuarioAutenticadoService.identificar(jwt.getSubject()), dados);
		return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
	}
}
