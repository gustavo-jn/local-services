package io.github.gustavo_jn.local_services_api.perfil;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import io.github.gustavo_jn.local_services_api.conta.entity.Usuario;
import io.github.gustavo_jn.local_services_api.conta.repository.UsuarioRepository;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest @AutoConfigureMockMvc
class PerfilConcorrenciaTests {
	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarios;
	@Autowired PasswordEncoder encoder;
	@Autowired JdbcTemplate jdbc;
	@Autowired JsonMapper json;
	@Test void duasCriacoesSimultaneasMantemUmPerfilEUmaAgenda() throws Exception {
		var usuario = usuarios.saveAndFlush(new Usuario("Teste concorrência", "perfil-concorrente-"+UUID.randomUUID()+"@example.com", encoder.encode("SenhaTeste123!")));
		var executor = Executors.newFixedThreadPool(2);
		try {
			var login = mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
					.content(json.writeValueAsString(Map.of("email", usuario.getEmail(), "senha", "SenhaTeste123!")))).andExpect(status().isOk()).andReturn();
			String token = login.getResponse().getHeader("Set-Cookie").split(";",2)[0].split("=",2)[1];
			String payload = json.writeValueAsString(Map.of("nome", "Profissional", "descricao", "Descrição para teste concorrente."));
			var inicio = new CountDownLatch(1);
			java.util.concurrent.Callable<Integer> criar = () -> {
				assertTrue(inicio.await(10, TimeUnit.SECONDS));
				return mvc.perform(post("/api/v1/me/perfil-profissional").with(csrf()).cookie(new Cookie("LS_ACCESS_TOKEN", token))
						.contentType(MediaType.APPLICATION_JSON).content(payload)).andReturn().getResponse().getStatus();
			};
			var a = executor.submit(criar); var b = executor.submit(criar); inicio.countDown();
			assertEquals(List.of(201,409), List.of(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS)).stream().sorted().toList());
			assertEquals(1L, jdbc.queryForObject("select count(*) from perfis_profissionais where usuario_id = ?",Long.class,usuario.getId()));
			assertEquals(1L, jdbc.queryForObject("select count(*) from agendas a join perfis_profissionais p on a.perfil_profissional_id=p.id where p.usuario_id = ?",Long.class,usuario.getId()));
		} finally {
			executor.shutdownNow(); executor.awaitTermination(10,TimeUnit.SECONDS);
			jdbc.update("delete from agendas where perfil_profissional_id in (select id from perfis_profissionais where usuario_id = ?)",usuario.getId());
			jdbc.update("delete from perfis_profissionais where usuario_id = ?",usuario.getId());
			jdbc.update("delete from usuarios where id = ?",usuario.getId());
		}
	}
}
