package io.github.gustavo_jn.local_services_api.auth.exception;

public class CredenciaisInvalidasException extends RuntimeException {

	public CredenciaisInvalidasException() {
		super("Credenciais inválidas");
	}
}
