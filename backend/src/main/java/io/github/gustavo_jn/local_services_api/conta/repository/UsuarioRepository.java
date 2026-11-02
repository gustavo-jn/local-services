package io.github.gustavo_jn.local_services_api.conta.repository;

import io.github.gustavo_jn.local_services_api.conta.entity.Usuario;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

	Optional<Usuario> findByEmail(String email);

	@Query("""
			select u.id as id, u.nome as nome, u.email as email, u.senhaHash as senhaHash
			from Usuario u
			where u.email = :email
			""")
	Optional<UsuarioCredenciaisProjection> findCredenciaisByEmail(@Param("email") String email);

	boolean existsByEmail(String email);

}
