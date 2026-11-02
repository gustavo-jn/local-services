package io.github.gustavo_jn.local_services_api.conta.service;

import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.gustavo_jn.local_services_api.common.exception.RegraNegocioException;
import io.github.gustavo_jn.local_services_api.conta.dto.*;
import io.github.gustavo_jn.local_services_api.conta.entity.Usuario;
import io.github.gustavo_jn.local_services_api.conta.exception.EmailJaCadastradoException;
import io.github.gustavo_jn.local_services_api.conta.repository.UsuarioRepository;

@Service
public class MinhaContaService {
	private final UsuarioRepository repository;
	private final PasswordEncoder encoder;
	public MinhaContaService(UsuarioRepository repository, PasswordEncoder encoder) {
		this.repository = repository;
		this.encoder = encoder;
	}
	@Transactional
	public UsuarioResponseDTO alterar(UUID id, AlterarContaRequestDTO dados) {
		Usuario usuario = obterParaAlterar(id);
		if (dados.nome() == null && dados.email() == null) {
			throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Informe o nome ou o e-mail para alterar");
		}
		if (dados.email() != null) {
			conferirSenha(usuario, dados.senhaAtual());
			if (!dados.email().equals(usuario.getEmail()) && repository.existsByEmail(dados.email())) {
				throw new EmailJaCadastradoException();
			}
			usuario.alterarEmail(dados.email());
		}
		if (dados.nome() != null) usuario.alterarNome(dados.nome());
		try {
			repository.flush();
		} catch (DataIntegrityViolationException exception) {
			for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
				if (cause instanceof org.hibernate.exception.ConstraintViolationException violation
						&& "uk_usuarios_email".equals(violation.getConstraintName())) throw new EmailJaCadastradoException(exception);
			}
			throw exception;
		}
		return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail());
	}
	@Transactional
	public void alterarSenha(UUID id, AlterarSenhaRequestDTO dados) {
		Usuario usuario = obterParaAlterar(id);
		conferirSenha(usuario, dados.senhaAtual());
		usuario.alterarSenhaHash(encoder.encode(dados.novaSenha()));
		repository.flush();
	}
	private Usuario obterParaAlterar(UUID id) {
		return repository.findByIdForUpdate(id).orElseThrow(() -> new BadCredentialsException("Autenticação necessária"));
	}
	private void conferirSenha(Usuario usuario, String senha) {
		var credenciais = repository.findCredenciaisByEmail(usuario.getEmail()).orElseThrow(
				() -> new BadCredentialsException("Autenticação necessária"));
		if (senha == null || !encoder.matches(senha, credenciais.getSenhaHash())) {
			throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "INVALID_CURRENT_PASSWORD", "Senha atual incorreta",
					Map.of("senhaAtual", "Confira sua senha atual"));
		}
	}
}
