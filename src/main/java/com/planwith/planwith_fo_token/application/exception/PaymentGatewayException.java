package com.planwith.planwith_fo_token.application.exception;

public class PaymentGatewayException extends RuntimeException {

	public PaymentGatewayException(String message) {
		super(message);
	}

	public PaymentGatewayException(String message, Throwable cause) {
		super(message, cause);
	}
}
