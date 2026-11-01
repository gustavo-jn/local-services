package io.github.gustavo_jn.local_services_api.common.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.gustavo_jn.local_services_api.conta.exception.EmailJaCadastradoException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EmailJaCadastradoException.class)
	public ResponseEntity<ApiError> handleEmailDuplicado(
			EmailJaCadastradoException exception, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(
				"EMAIL_ALREADY_REGISTERED", "E-mail já cadastrado", Map.of("email", "E-mail já cadastrado"),
				Instant.now(), request.getRequestURI()));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiError> handleConstraintValidation(
			ConstraintViolationException exception, HttpServletRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getConstraintViolations().forEach(violation ->
				fieldErrors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage()));
		return ResponseEntity.badRequest().body(new ApiError(
				"VALIDATION_ERROR", "Dados inválidos", fieldErrors,
				Instant.now(), request.getRequestURI()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(
			MethodArgumentNotValidException exception, HttpServletRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
				fieldErrors.putIfAbsent(error.getField(),
						error.getDefaultMessage() != null ? error.getDefaultMessage() : "Valor inválido"));

		return ResponseEntity.badRequest().body(new ApiError(
				"VALIDATION_ERROR", "Dados inválidos", fieldErrors,
				Instant.now(), request.getRequestURI()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleUnreadableMessage(
			HttpMessageNotReadableException exception, HttpServletRequest request) {
		return ResponseEntity.badRequest().body(new ApiError(
				"INVALID_REQUEST_BODY", "Corpo da requisição ausente ou inválido",
				Map.of(), Instant.now(), request.getRequestURI()));
	}

}
