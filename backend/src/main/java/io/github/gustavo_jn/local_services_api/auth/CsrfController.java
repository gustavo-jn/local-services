package io.github.gustavo_jn.local_services_api.auth;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

	@GetMapping("/api/v1/auth/csrf")
	public CsrfResponse csrf(CsrfToken csrfToken) {
		return new CsrfResponse(csrfToken.getHeaderName(), csrfToken.getToken());
	}

	public record CsrfResponse(String headerName, String token) {
	}

}
