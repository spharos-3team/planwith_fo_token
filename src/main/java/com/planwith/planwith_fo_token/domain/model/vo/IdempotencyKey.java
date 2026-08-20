package com.planwith.planwith_fo_token.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * API/Kafka 요청 멱등성 키. 현재는 {@link TransactionUuid}와 동일 값을 사용한다.
 */
public record IdempotencyKey(UUID value) {

	public IdempotencyKey {
		Objects.requireNonNull(value, "Idempotency key is required.");
	}

	public static IdempotencyKey from(TransactionUuid transactionUuid) {
		return new IdempotencyKey(transactionUuid.value());
	}

	public TransactionUuid toTransactionUuid() {
		return new TransactionUuid(value);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
