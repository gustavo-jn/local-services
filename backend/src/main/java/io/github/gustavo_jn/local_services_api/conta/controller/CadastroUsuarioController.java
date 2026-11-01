package io.github.gustavo_jn.local_services_api.conta.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.github.gustavo_jn.local_services_api.conta.dto.CadastroUsuarioRequestDTO;
import io.github.gustavo_jn.local_services_api.conta.dto.UsuarioResponseDTO;
import io.github.gustavo_jn.local_services_api.conta.service.UsuarioService;

@RestController
public class CadastroUsuarioController {

	private final UsuarioService usuarioService;

	public CadastroUsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@PostMapping("/api/v1/auth/register")
	public ResponseEntity<UsuarioResponseDTO> cadastrar(@RequestBody CadastroUsuarioRequestDTO cadastro) {
		return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.cadastrar(cadastro));
	}

}
