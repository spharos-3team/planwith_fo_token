package com.planwith.planwith_fo_token.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login response")
public record LoginResponse(
		@Schema(description = "User ID", example = "test-001")
		String id,

		@Schema(description = "Result message")
		String message
) {
}
