package com.planwith.planwith_fo_token.adapter.in.web.dto;

import java.util.UUID;

public record TokenCommandRequest(
		UUID transactionUuid,
		long amount,
		String referenceType,
		String referenceUuid,
		String description
) {
}
