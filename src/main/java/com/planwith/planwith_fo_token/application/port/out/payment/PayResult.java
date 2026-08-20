package com.planwith.planwith_fo_token.application.port.out.payment;

import java.time.Instant;

public record PayResult(
		String paymentId,
		String status,
		Instant paidAt
) {
}
