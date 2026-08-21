package com.planwith.planwith_fo_token.application.query;

import java.time.Instant;
import java.util.UUID;

public record PaymentMethodResult(
		UUID paymentMethodUuid,
		String cardName,
		String fourCardNumber,
		boolean defaultMethod,
		Instant registeredAt
) {
}
