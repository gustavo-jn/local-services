package io.github.gustavo_jn.local_services_api.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import io.github.gustavo_jn.local_services_api.conta.dto.CadastroUsuarioRequestDTO;
import io.github.gustavo_jn.local_services_api.conta.repository.UsuarioRepository;
import io.github.gustavo_jn.local_services_api.conta.service.UsuarioService;
import io.github.gustavo_jn.local_services_api.perfil.dto.PerfilProfissionalRequestDTO;
import io.github.gustavo_jn.local_services_api.perfil.repository.PerfilProfissionalRepository;
import io.github.gustavo_jn.local_services_api.perfil.service.PerfilProfissionalService;

@Configuration
@Profile("demo")
public class DemoDataConfiguration {
	@Bean
	ApplicationRunner prepararDemonstracao(UsuarioRepository usuarios, UsuarioService contas,
			PerfilProfissionalRepository perfis, PerfilProfissionalService profissionais) {
		return args -> {
			if (!usuarios.existsByEmail("marina.demo@example.com")) {
				contas.cadastrar(new CadastroUsuarioRequestDTO("Marina Exemplo", "marina.demo@example.com", "Demo12345!"));
			}
			if (!usuarios.existsByEmail("pedro.demo@example.com")) {
				contas.cadastrar(new CadastroUsuarioRequestDTO("Pedro Exemplo", "pedro.demo@example.com", "Demo12345!"));
			}
			var pedro = usuarios.findByEmail("pedro.demo@example.com").orElseThrow();
			if (!perfis.existsByUsuarioId(pedro.getId())) {
				profissionais.criar(pedro.getId(), new PerfilProfissionalRequestDTO("Pedro — serviços locais",
						"Perfil fictício para apresentar o cadastro e a edição de informações profissionais.", true));
			}
		};
	}
}
