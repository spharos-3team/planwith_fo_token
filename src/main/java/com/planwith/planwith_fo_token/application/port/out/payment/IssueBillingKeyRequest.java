package com.planwith.planwith_fo_token.application.port.out.payment;

public record IssueBillingKeyRequest(
		String customerId,
		String channelKey,
		CardCredential cardCredential
) {
}
