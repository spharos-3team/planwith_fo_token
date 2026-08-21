package com.planwith.planwith_fo_token.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

public record TokenChargeResponse(
		UUID chargeUuid,
		String productCode,
		String status,
		long tokenAmount,
		long paidAmount,
		UUID paymentMethodUuid,
		String paymentType,
		Instant createdAt
) {
}
