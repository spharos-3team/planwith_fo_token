package com.planwith.planwith_fo_token.application.service.support;

import com.planwith.planwith_fo_token.application.query.PaymentMethodResult;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;

public final class PaymentMethodResultMapper {

	private PaymentMethodResultMapper() {
	}

	public static PaymentMethodResult toResult(PaymentMethod method) {
		return new PaymentMethodResult(
				method.paymentMethodUuid().value(),
				method.cardName(),
				method.fourCardNumber(),
				method.defaultMethod(),
				method.registeredAt()
		);
	}
}
