package io.github.gustavo_jn.local_services_api.perfil.entity;

import java.util.UUID;
import jakarta.persistence.*;
import io.github.gustavo_jn.local_services_api.conta.entity.Usuario;

@Entity
@Table(name = "perfis_profissionais")
public class PerfilProfissional {
	@Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false, unique = true) private Usuario usuario;
	@Column(nullable = false, length = 120) private String nome;
	@Column(nullable = false, length = 3000) private String descricao;
	@Column(name = "aceita_novos_servicos", nullable = false) private boolean aceitaNovosServicos = true;
	@Column(nullable = false) private boolean publicado = false;
	protected PerfilProfissional() { }
	public PerfilProfissional(Usuario usuario, String nome, String descricao) {
		this.usuario = usuario; this.nome = nome; this.descricao = descricao;
	}
	public void alterar(String nome, String descricao, Boolean aceita) {
		this.nome = nome; this.descricao = descricao;
		if (aceita != null) this.aceitaNovosServicos = aceita;
	}
	public UUID getId() { return id; }
	public String getNome() { return nome; }
	public String getDescricao() { return descricao; }
	public boolean isAceitaNovosServicos() { return aceitaNovosServicos; }
	public boolean isPublicado() { return publicado; }
}
