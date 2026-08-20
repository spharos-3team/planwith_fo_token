package com.planwith.planwith_fo_token.application.port.out.payment;

public record CancelPaymentRequest(
		String paymentId,
		String reason,
		Long cancelAmount
) {
}
