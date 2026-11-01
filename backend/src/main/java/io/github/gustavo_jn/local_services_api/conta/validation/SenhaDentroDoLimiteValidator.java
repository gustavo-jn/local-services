package io.github.gustavo_jn.local_services_api.conta.validation;

import java.nio.charset.StandardCharsets;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SenhaDentroDoLimiteValidator implements ConstraintValidator<SenhaDentroDoLimite, String> {

	@Override
	public boolean isValid(String senha, ConstraintValidatorContext context) {
		return senha == null || senha.getBytes(StandardCharsets.UTF_8).length <= 72;
	}

}
