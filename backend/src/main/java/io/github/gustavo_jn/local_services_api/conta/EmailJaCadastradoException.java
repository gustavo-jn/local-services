package io.github.gustavo_jn.local_services_api.conta;

public class EmailJaCadastradoException extends RuntimeException {

	public EmailJaCadastradoException() {
		super("E-mail já cadastrado");
	}

	public EmailJaCadastradoException(Throwable cause) {
		super("E-mail já cadastrado", cause);
	}

}
