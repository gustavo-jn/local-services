package io.github.gustavo_jn.local_services_api.conta.repository;

import io.github.gustavo_jn.local_services_api.conta.entity.Usuario;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

	Optional<Usuario> findByEmail(String email);

	boolean existsByEmail(String email);

}
