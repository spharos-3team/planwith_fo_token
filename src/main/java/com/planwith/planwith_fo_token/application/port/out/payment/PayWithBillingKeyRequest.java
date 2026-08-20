package com.planwith.planwith_fo_token.application.port.out.payment;

public record PayWithBillingKeyRequest(
		String paymentId,
		String billingKey,
		String orderName,
		long totalAmount,
		String currency,
		String channelKey
) {
}
