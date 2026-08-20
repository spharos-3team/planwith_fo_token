package com.planwith.planwith_fo_token.domain.exception;

public class DuplicateTransactionException extends RuntimeException {

	public DuplicateTransactionException(String message) {
		super(message);
	}
}
