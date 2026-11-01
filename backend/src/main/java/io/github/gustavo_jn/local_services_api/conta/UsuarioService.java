package io.github.gustavo_jn.local_services_api.conta;

import java.util.Locale;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final Validator validator;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, Validator validator) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.validator = validator;
	}

	@Transactional
	public UsuarioResponseDTO cadastrar(CadastroUsuarioRequestDTO cadastro) {
		String email = cadastro.email() == null ? null : cadastro.email().trim().toLowerCase(Locale.ROOT);
		CadastroUsuarioRequestDTO normalizado = new CadastroUsuarioRequestDTO(cadastro.nome(), email, cadastro.senha());
		var violations = validator.validate(normalizado);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}

		if (usuarioRepository.existsByEmail(email)) {
			throw new EmailJaCadastradoException();
		}

		Usuario usuario = new Usuario(normalizado.nome(), email, passwordEncoder.encode(normalizado.senha()));
		try {
			usuario = usuarioRepository.saveAndFlush(usuario);
		} catch (DataIntegrityViolationException exception) {
			if (isEmailDuplicado(exception)) {
				throw new EmailJaCadastradoException(exception);
			}
			throw exception;
		}
		return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail());
	}

	private boolean isEmailDuplicado(Throwable exception) {
		for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
			if (cause instanceof org.hibernate.exception.ConstraintViolationException violation
					&& "uk_usuarios_email".equals(violation.getConstraintName())) {
				return true;
			}
		}
		return false;
	}

}
