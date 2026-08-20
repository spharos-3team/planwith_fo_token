package com.planwith.planwith_fo_token.adapter.in.web.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API error response")
public record ApiErrorResponse(
		@Schema(description = "Error timestamp")
		Instant timestamp,

		@Schema(description = "HTTP status code")
		int status,

		@Schema(description = "Error code")
		String code,

		@Schema(description = "Error message")
		String message
) {
}
