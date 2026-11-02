package io.github.gustavo_jn.local_services_api.common.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class RegraNegocioException extends RuntimeException {
	private final HttpStatus status;
	private final String code;
	private final Map<String, String> fieldErrors;

	public RegraNegocioException(HttpStatus status, String code, String message) {
		this(status, code, message, Map.of());
	}
	public RegraNegocioException(HttpStatus status, String code, String message, Map<String, String> fields) {
		super(message);
		this.status = status;
		this.code = code;
		this.fieldErrors = Map.copyOf(fields);
	}
	public HttpStatus getStatus() { return status; }
	public String getCode() { return code; }
	public Map<String, String> getFieldErrors() { return fieldErrors; }
}
