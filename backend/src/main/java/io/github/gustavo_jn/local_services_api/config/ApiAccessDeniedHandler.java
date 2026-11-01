package io.github.gustavo_jn.local_services_api.config;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import io.github.gustavo_jn.local_services_api.common.exception.ApiError;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

	private final JsonMapper jsonMapper;

	public ApiAccessDeniedHandler(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException exception) throws IOException {
		boolean csrfFailure = exception instanceof CsrfException;
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		jsonMapper.writeValue(response.getOutputStream(), new ApiError(
				csrfFailure ? "INVALID_CSRF_TOKEN" : "ACCESS_DENIED",
				csrfFailure ? "Token CSRF ausente ou inválido" : "Acesso não permitido",
				Map.of(), Instant.now(), request.getRequestURI()));
	}

}
