package com.planwith.planwith_fo_token.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public record ChargeUuid(UUID value) {

	public ChargeUuid {
		Objects.requireNonNull(value, "Charge UUID is required.");
	}

	public static ChargeUuid from(String value) {
		return new ChargeUuid(UUID.fromString(value));
	}

	public static ChargeUuid newId() {
		return new ChargeUuid(UUID.randomUUID());
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
