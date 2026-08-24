package com.planwith.planwith_fo_token.adapter.in.web.auth;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GatewayMemberAuthorizationFilter extends OncePerRequestFilter {

	public static final String AUTHENTICATED_MEMBER_HEADER = "X-Auth-User-Id";

	private static final Pattern MEMBER_API_PATTERN = Pattern.compile(
			"^/api/planwith-fo-token/members/([0-9a-fA-F-]{36})(?:/.*)?$"
	);

	private final boolean enabled;

	public GatewayMemberAuthorizationFilter(
			@Value("${token.gateway-auth.enabled:true}") boolean enabled
	) {
		this.enabled = enabled;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		if (!enabled) {
			filterChain.doFilter(request, response);
			return;
		}

		Matcher matcher = MEMBER_API_PATTERN.matcher(request.getRequestURI());
		if (!matcher.matches()) {
			filterChain.doFilter(request, response);
			return;
		}

		UUID pathMemberUuid = parseUuid(matcher.group(1));
		UUID authenticatedMemberUuid = parseUuid(request.getHeader(AUTHENTICATED_MEMBER_HEADER));
		if (authenticatedMemberUuid == null) {
			log.warn("GatewayMemberAuthorizationFilter : doFilterInternal : 인증 회원 식별자 누락");
			writeError(response, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "인증이 필요합니다.");
			return;
		}
		if (pathMemberUuid == null || !pathMemberUuid.equals(authenticatedMemberUuid)) {
			log.warn(
					"GatewayMemberAuthorizationFilter : doFilterInternal : 다른 회원의 토큰 자원 접근 차단 - pathMemberUuid={}, authenticatedMemberUuid={}",
					pathMemberUuid,
					authenticatedMemberUuid
			);
			writeError(response, HttpStatus.FORBIDDEN, "TOKEN_MEMBER_ACCESS_DENIED", "다른 회원의 토큰 자원에 접근할 수 없습니다.");
			return;
		}

		filterChain.doFilter(request, response);
	}

	private static UUID parseUuid(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

	private static void writeError(
			HttpServletResponse response,
			HttpStatus status,
			String code,
			String message
	) throws IOException {
		response.setStatus(status.value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
	}
}
