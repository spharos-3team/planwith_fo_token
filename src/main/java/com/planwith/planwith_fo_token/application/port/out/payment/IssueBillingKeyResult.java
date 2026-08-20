package com.planwith.planwith_fo_token.application.port.out.payment;

public record IssueBillingKeyResult(
		String billingKey,
		String cardName,
		String fourCardNumber
) {
}
