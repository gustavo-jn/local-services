package io.github.gustavo_jn.local_services_api.conta.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, length = 120)
	private String nome;

	@Column(nullable = false, unique = true, length = 254)
	private String email;

	@Column(name = "senha_hash", nullable = false, length = 60)
	private String senhaHash;

	protected Usuario() {
	}

	public Usuario(String nome, String email, String senhaHash) {
		this.nome = nome;
		this.email = email;
		this.senhaHash = senhaHash;
	}

	public UUID getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getEmail() {
		return email;
	}

	String getSenhaHash() {
		return senhaHash;
	}

	public void alterarNome(String nome) { this.nome = nome; }
	public void alterarEmail(String email) { this.email = email; }
	public void alterarSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

}
