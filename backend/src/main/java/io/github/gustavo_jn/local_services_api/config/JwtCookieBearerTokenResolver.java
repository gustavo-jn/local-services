package io.github.gustavo_jn.local_services_api.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtCookieBearerTokenResolver implements BearerTokenResolver {

	public static final String COOKIE_NAME = "LS_ACCESS_TOKEN";

	@Override
	public String resolve(HttpServletRequest request) {
		String path = request.getServletPath();
		if (!StringUtils.hasText(path)) {
			path = request.getRequestURI().substring(request.getContextPath().length());
		}
		if (isPublicAuthenticationPath(path)) {
			return null;
		}

		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}

		String token = null;
		for (Cookie cookie : cookies) {
			if (!COOKIE_NAME.equals(cookie.getName())) {
				continue;
			}
			if (token != null || !StringUtils.hasText(cookie.getValue())) {
				return null;
			}
			token = cookie.getValue();
		}
		return token;
	}

	private boolean isPublicAuthenticationPath(String path) {
		return "/api/v1/health".equals(path)
				|| "/api/v1/auth/csrf".equals(path)
				|| "/api/v1/auth/register".equals(path)
				|| "/api/v1/auth/login".equals(path)
				|| "/api/v1/auth/logout".equals(path);
	}
}
