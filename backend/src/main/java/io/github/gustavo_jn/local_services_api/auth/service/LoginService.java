package io.github.gustavo_jn.local_services_api.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.github.gustavo_jn.local_services_api.auth.dto.LoginRequestDTO;
import io.github.gustavo_jn.local_services_api.auth.exception.CredenciaisInvalidasException;
import io.github.gustavo_jn.local_services_api.conta.dto.UsuarioResponseDTO;
import io.github.gustavo_jn.local_services_api.conta.repository.UsuarioCredenciaisProjection;
import io.github.gustavo_jn.local_services_api.conta.repository.UsuarioRepository;

@Service
public class LoginService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtEncoder jwtEncoder;
	private final String issuer;
	private final String audience;
	private final Duration expiration;

	public LoginService(
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder,
			JwtEncoder jwtEncoder,
			@Value("${app.security.jwt.issuer}") String issuer,
			@Value("${app.security.jwt.audience}") String audience,
			@Value("${app.security.jwt.expiration}") Duration expiration) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtEncoder = jwtEncoder;
		this.issuer = issuer;
		this.audience = audience;
		this.expiration = expiration;
	}

	public ResultadoLogin autenticar(LoginRequestDTO request) {
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		UsuarioCredenciaisProjection usuario = usuarioRepository.findCredenciaisByEmail(email)
				.orElseThrow(CredenciaisInvalidasException::new);

		if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
			throw new CredenciaisInvalidasException();
		}

		Instant agora = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(issuer)
				.subject(usuario.getId().toString())
				.audience(List.of(audience))
				.issuedAt(agora)
				.expiresAt(agora.plus(expiration))
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		UsuarioResponseDTO resposta = new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail());
		return new ResultadoLogin(token, resposta);
	}

	public record ResultadoLogin(String token, UsuarioResponseDTO usuario) {
	}
}
