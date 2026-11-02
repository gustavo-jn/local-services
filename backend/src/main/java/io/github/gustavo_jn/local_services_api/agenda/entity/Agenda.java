package io.github.gustavo_jn.local_services_api.agenda.entity;

import java.util.UUID;
import jakarta.persistence.*;
import io.github.gustavo_jn.local_services_api.perfil.entity.PerfilProfissional;

@Entity @Table(name = "agendas")
public class Agenda {
	@Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "perfil_profissional_id", nullable = false, unique = true) private PerfilProfissional perfilProfissional;
	@Column(name = "fuso_referencia", nullable = false, length = 60) private String fusoReferencia = "America/Sao_Paulo";
	protected Agenda() { }
	public Agenda(PerfilProfissional perfil) { this.perfilProfissional = perfil; }
	public UUID getId() { return id; }
	public String getFusoReferencia() { return fusoReferencia; }
}
