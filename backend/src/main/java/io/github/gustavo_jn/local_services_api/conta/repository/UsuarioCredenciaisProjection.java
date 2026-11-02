package io.github.gustavo_jn.local_services_api.conta.repository;

import java.util.UUID;

public interface UsuarioCredenciaisProjection {

	UUID getId();

	String getNome();

	String getEmail();

	String getSenhaHash();
}
