package com.planwith.planwith_fo_token.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public record PaymentMethodUuid(UUID value) {

	public PaymentMethodUuid {
		Objects.requireNonNull(value, "Payment method UUID is required.");
	}

	public static PaymentMethodUuid from(String value) {
		return new PaymentMethodUuid(UUID.fromString(value));
	}

	public static PaymentMethodUuid newId() {
		return new PaymentMethodUuid(UUID.randomUUID());
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
