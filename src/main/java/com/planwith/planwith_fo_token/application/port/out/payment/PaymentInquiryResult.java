package com.planwith.planwith_fo_token.application.port.out.payment;

import java.time.Instant;

public record PaymentInquiryResult(
		String paymentId,
		String status,
		long totalAmount,
		String billingKey,
		Instant paidAt
) {
}
