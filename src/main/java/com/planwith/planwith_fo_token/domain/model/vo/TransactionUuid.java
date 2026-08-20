package com.planwith.planwith_fo_token.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public record TransactionUuid(UUID value) {

	public TransactionUuid {
		Objects.requireNonNull(value, "Transaction UUID is required.");
	}

	public static TransactionUuid from(String value) {
		return new TransactionUuid(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
