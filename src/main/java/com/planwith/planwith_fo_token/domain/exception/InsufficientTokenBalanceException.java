package com.planwith.planwith_fo_token.domain.exception;

public class InsufficientTokenBalanceException extends RuntimeException {

	public InsufficientTokenBalanceException(String message) {
		super(message);
	}
}
