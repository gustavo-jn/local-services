package io.github.gustavo_jn.local_services_api.agenda.repository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import io.github.gustavo_jn.local_services_api.agenda.entity.Agenda;
public interface AgendaRepository extends JpaRepository<Agenda, UUID> {
	Optional<Agenda> findByPerfilProfissionalId(UUID perfilId);
}
