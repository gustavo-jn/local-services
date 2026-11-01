package io.github.gustavo_jn.local_services_api.common.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
		String code,
		String message,
		Map<String, String> fieldErrors,
		Instant timestamp,
		String path) {
}
