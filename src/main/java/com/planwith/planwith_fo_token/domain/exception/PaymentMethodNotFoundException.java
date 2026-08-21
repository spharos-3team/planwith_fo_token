package com.planwith.planwith_fo_token.domain.exception;

public class PaymentMethodNotFoundException extends RuntimeException {

	public PaymentMethodNotFoundException(String message) {
		super(message);
	}
}
