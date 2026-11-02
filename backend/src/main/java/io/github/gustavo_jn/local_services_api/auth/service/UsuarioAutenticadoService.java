package io.github.gustavo_jn.local_services_api.auth.service;

import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.gustavo_jn.local_services_api.conta.dto.UsuarioResponseDTO;
import io.github.gustavo_jn.local_services_api.conta.repository.UsuarioRepository;

@Service
public class UsuarioAutenticadoService {

	private final UsuarioRepository usuarioRepository;

	public UsuarioAutenticadoService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional(readOnly = true)
	public UsuarioResponseDTO obter(String subject) {
		UUID id = identificar(subject);
		return usuarioRepository.findById(id)
				.map(usuario -> new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail()))
				.orElseThrow(() -> new BadCredentialsException("Autenticação necessária"));
	}

	public static UUID identificar(String subject) {
		final UUID id;
		try {
			if (subject == null) {
				throw new IllegalArgumentException();
			}
			id = UUID.fromString(subject);
		} catch (IllegalArgumentException exception) {
			throw new BadCredentialsException("Autenticação necessária");
		}

		return id;
	}
}
