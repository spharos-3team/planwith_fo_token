package com.planwith.planwith_fo_token.application.port.out.payment;

public record PayRequest(
		String paymentId,
		String orderName,
		long totalAmount,
		String currency,
		String channelKey
) {
}
