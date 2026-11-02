package io.github.gustavo_jn.local_services_api.perfil.repository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import io.github.gustavo_jn.local_services_api.perfil.entity.PerfilProfissional;
public interface PerfilProfissionalRepository extends JpaRepository<PerfilProfissional, UUID> {
	Optional<PerfilProfissional> findByUsuarioId(UUID usuarioId);
	boolean existsByUsuarioId(UUID usuarioId);
}
