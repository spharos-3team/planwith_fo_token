package com.planwith.planwith_fo_token.application.port.out;

public record TokenOutboxMessage(
		String eventUuid,
		String aggregateType,
		String aggregateUuid,
		String eventType,
		String payload
) {
}
