package io.github.gustavo_jn.local_services_api.perfil.service;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.gustavo_jn.local_services_api.agenda.entity.Agenda;
import io.github.gustavo_jn.local_services_api.agenda.repository.AgendaRepository;
import io.github.gustavo_jn.local_services_api.common.exception.RegraNegocioException;
import io.github.gustavo_jn.local_services_api.conta.repository.UsuarioRepository;
import io.github.gustavo_jn.local_services_api.perfil.dto.*;
import io.github.gustavo_jn.local_services_api.perfil.entity.PerfilProfissional;
import io.github.gustavo_jn.local_services_api.perfil.repository.PerfilProfissionalRepository;

@Service
public class PerfilProfissionalService {
	private final UsuarioRepository usuarios;
	private final PerfilProfissionalRepository perfis;
	private final AgendaRepository agendas;
	public PerfilProfissionalService(UsuarioRepository usuarios, PerfilProfissionalRepository perfis, AgendaRepository agendas) {
		this.usuarios = usuarios; this.perfis = perfis; this.agendas = agendas;
	}
	@Transactional
	public PerfilProfissionalResponseDTO criar(UUID usuarioId, PerfilProfissionalRequestDTO dados) {
		var usuario = usuarios.findByIdForUpdate(usuarioId).orElseThrow(() -> new BadCredentialsException("Autenticação necessária"));
		if (perfis.existsByUsuarioId(usuarioId)) throw new RegraNegocioException(HttpStatus.CONFLICT, "PROFILE_ALREADY_EXISTS", "Você já possui um perfil profissional");
		var perfil = new PerfilProfissional(usuario, dados.nome(), dados.descricao());
		perfil.alterar(dados.nome(), dados.descricao(), dados.aceitaNovosServicos());
		perfis.saveAndFlush(perfil);
		var agenda = agendas.saveAndFlush(new Agenda(perfil));
		return resposta(perfil, agenda);
	}
	@Transactional(readOnly = true)
	public PerfilProfissionalResponseDTO obter(UUID usuarioId) {
		conferirUsuario(usuarioId);
		var perfil = proprio(usuarioId);
		return resposta(perfil, agendas.findByPerfilProfissionalId(perfil.getId()).orElseThrow());
	}
	@Transactional
	public PerfilProfissionalResponseDTO alterar(UUID usuarioId, PerfilProfissionalRequestDTO dados) {
		usuarios.findByIdForUpdate(usuarioId).orElseThrow(() -> new BadCredentialsException("Autenticação necessária"));
		var perfil = proprio(usuarioId);
		perfil.alterar(dados.nome(), dados.descricao(), dados.aceitaNovosServicos());
		perfis.flush();
		return resposta(perfil, agendas.findByPerfilProfissionalId(perfil.getId()).orElseThrow());
	}
	private PerfilProfissional proprio(UUID usuarioId) {
		return perfis.findByUsuarioId(usuarioId).orElseThrow(() -> new RegraNegocioException(HttpStatus.NOT_FOUND, "PROFILE_NOT_FOUND", "Você ainda não possui perfil profissional"));
	}
	private void conferirUsuario(UUID id) {
		if (!usuarios.existsById(id)) throw new BadCredentialsException("Autenticação necessária");
	}
	private PerfilProfissionalResponseDTO resposta(PerfilProfissional perfil, Agenda agenda) {
		return new PerfilProfissionalResponseDTO(perfil.getId(), perfil.getNome(), perfil.getDescricao(), perfil.isAceitaNovosServicos(),
				perfil.isPublicado(), new PerfilProfissionalResponseDTO.AgendaDTO(agenda.getId(), agenda.getFusoReferencia()));
	}
}
