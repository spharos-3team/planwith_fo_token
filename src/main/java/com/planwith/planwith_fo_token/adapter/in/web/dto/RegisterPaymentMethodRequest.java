package com.planwith.planwith_fo_token.adapter.in.web.dto;

public record RegisterPaymentMethodRequest(
		String cardNumber,
		String expiryYear,
		String expiryMonth,
		String birthOrBusinessRegistrationNumber,
		String passwordTwoDigits,
		Boolean defaultMethod
) {
}
