package com.planwith.planwith_fo_token.application.port.out;

import java.time.Instant;

public record TokenOutboxMessage(
		String eventUuid,
		String aggregateType,
		String aggregateUuid,
		String eventType,
		String payload,
		Instant occurredAt
) {
}
