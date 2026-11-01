package io.github.gustavo_jn.local_services_api.conta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = SenhaDentroDoLimiteValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SenhaDentroDoLimite {

	String message() default "A senha deve ter no máximo 72 bytes em UTF-8";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
