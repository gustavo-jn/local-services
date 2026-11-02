package io.github.gustavo_jn.local_services_api.perfil.controller;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import io.github.gustavo_jn.local_services_api.auth.service.UsuarioAutenticadoService;
import io.github.gustavo_jn.local_services_api.perfil.dto.*;
import io.github.gustavo_jn.local_services_api.perfil.service.PerfilProfissionalService;

@RestController @RequestMapping("/api/v1/me/perfil-profissional")
public class PerfilProfissionalController {
	private final PerfilProfissionalService service;
	public PerfilProfissionalController(PerfilProfissionalService service) { this.service = service; }
	@GetMapping
	public ResponseEntity<PerfilProfissionalResponseDTO> obter(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.obter(UsuarioAutenticadoService.identificar(jwt.getSubject())));
	}
	@PostMapping
	public ResponseEntity<PerfilProfissionalResponseDTO> criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PerfilProfissionalRequestDTO dados) {
		return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore()).body(service.criar(UsuarioAutenticadoService.identificar(jwt.getSubject()), dados));
	}
	@PatchMapping
	public ResponseEntity<PerfilProfissionalResponseDTO> alterar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PerfilProfissionalRequestDTO dados) {
		return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.alterar(UsuarioAutenticadoService.identificar(jwt.getSubject()), dados));
	}
}
