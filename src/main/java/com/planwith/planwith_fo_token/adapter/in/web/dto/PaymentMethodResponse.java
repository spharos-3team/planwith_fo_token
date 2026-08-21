package com.planwith.planwith_fo_token.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

public record PaymentMethodResponse(
		UUID paymentMethodUuid,
		String cardName,
		String fourCardNumber,
		boolean defaultMethod,
		Instant registeredAt
) {
}
