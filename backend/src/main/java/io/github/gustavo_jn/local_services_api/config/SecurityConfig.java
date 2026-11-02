package io.github.gustavo_jn.local_services_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public CsrfTokenRepository csrfTokenRepository() {
		return new HttpSessionCsrfTokenRepository();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
			ApiAuthenticationEntryPoint authenticationEntryPoint,
			ApiAccessDeniedHandler accessDeniedHandler,
			CsrfTokenRepository csrfTokenRepository,
			JwtCookieBearerTokenResolver bearerTokenResolver) throws Exception {
		return http
				.csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository)
						.withObjectPostProcessor(new ObjectPostProcessor<CsrfFilter>() {
							@Override
							public <O extends CsrfFilter> O postProcess(O filter) {
								// O Resource Server dispensa CSRF para bearer tokens por padrão.
								// Nosso JWT vem de cookie: todas as operações inseguras exigem CSRF.
								filter.setRequireCsrfProtectionMatcher(CsrfFilter.DEFAULT_CSRF_MATCHER);
								filter.setAccessDeniedHandler(accessDeniedHandler);
								return filter;
							}
						}))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.GET, "/api/v1/health").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/auth/csrf").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()
						.anyRequest().authenticated())
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.oauth2ResourceServer(resourceServer -> resourceServer
						.bearerTokenResolver(bearerTokenResolver)
						.jwt(Customizer.withDefaults())
						.authenticationEntryPoint(authenticationEntryPoint))
				.build();
	}

}
