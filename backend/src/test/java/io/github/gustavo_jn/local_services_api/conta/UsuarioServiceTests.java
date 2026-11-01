package io.github.gustavo_jn.local_services_api.conta;

import java.sql.SQLException;
import java.util.UUID;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTests {

	private final UsuarioRepository repository = mock(UsuarioRepository.class);
	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
	private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private final UsuarioService service = new UsuarioService(repository, encoder, factory.getValidator());

	@AfterEach
	void fecharValidator() {
		factory.close();
	}

	@Test
	void normalizaEmailSalvaHashEPreservaSenhaComEspacos() {
		UUID id = UUID.randomUUID();
		String senha = " senha de teste ";
		when(repository.saveAndFlush(any(Usuario.class))).thenAnswer(invocation -> {
			Usuario usuario = invocation.getArgument(0);
			assertTrue(encoder.matches(senha, usuario.getSenhaHash()));
			assertFalse(encoder.matches(senha.trim(), usuario.getSenhaHash()));
			ReflectionTestUtils.setField(usuario, "id", id);
			return usuario;
		});

		UsuarioResponseDTO resposta = service.cadastrar(
				new CadastroUsuarioRequestDTO("Usuario Teste", "  USUARIO@Example.COM  ", senha));

		assertEquals(new UsuarioResponseDTO(id, "Usuario Teste", "usuario@example.com"), resposta);
		verify(repository).existsByEmail("usuario@example.com");
		verify(repository, times(1)).saveAndFlush(any(Usuario.class));
	}

	@Test
	void rejeitaEmailExistenteSemSalvar() {
		when(repository.existsByEmail("usuario@example.com")).thenReturn(true);
		assertThrows(EmailJaCadastradoException.class, () -> service.cadastrar(cadastro("Senha123!")));
		verify(repository, never()).saveAndFlush(any());
	}

	@Test
	void traduzConflitoDeEmailDetectadoPeloBanco() {
		var causa = new org.hibernate.exception.ConstraintViolationException(
				"Conflito de unicidade", new SQLException("duplicate", "23505"), "uk_usuarios_email");
		when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("Conflito", causa));
		assertThrows(EmailJaCadastradoException.class, () -> service.cadastrar(cadastro("Senha123!")));
	}

	@Test
	void naoClassificaOutrasFalhasDoBancoComoEmailDuplicado() {
		var falha = new DataIntegrityViolationException("Outro conflito");
		when(repository.saveAndFlush(any())).thenThrow(falha);
		assertSame(falha, assertThrows(DataIntegrityViolationException.class,
				() -> service.cadastrar(cadastro("Senha123!"))));
	}

	@Test
	void rejeitaSenhaForaDoLimiteAntesDeAcessarBanco() {
		assertThrows(ConstraintViolationException.class, () -> service.cadastrar(cadastro("á".repeat(37))));
		assertThrows(ConstraintViolationException.class, () -> service.cadastrar(cadastro("curta")));
		verifyNoInteractions(repository);
	}

	@Test
	void rejeitaCamposInvalidosAntesDeAcessarBanco() {
		assertThrows(ConstraintViolationException.class, () -> service.cadastrar(
				new CadastroUsuarioRequestDTO(" ", "email-invalido", "Senha123!")));
		verifyNoInteractions(repository);
	}

	private CadastroUsuarioRequestDTO cadastro(String senha) {
		return new CadastroUsuarioRequestDTO("Usuario Teste", "usuario@example.com", senha);
	}

}
