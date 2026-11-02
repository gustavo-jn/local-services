package io.github.gustavo_jn.local_services_api.conta.repository;

import io.github.gustavo_jn.local_services_api.conta.entity.Usuario;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

	Optional<Usuario> findByEmail(String email);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from Usuario u where u.id = :id")
	Optional<Usuario> findByIdForUpdate(@Param("id") UUID id);

	@Query("""
			select u.id as id, u.nome as nome, u.email as email, u.senhaHash as senhaHash
			from Usuario u
			where u.email = :email
			""")
	Optional<UsuarioCredenciaisProjection> findCredenciaisByEmail(@Param("email") String email);

	boolean existsByEmail(String email);

}
