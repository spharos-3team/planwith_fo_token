package com.planwith.planwith_fo_token.application.event;

/**
 * Token Service가 외부 MSA에 발행하는 Output Event 목록.
 * 실제 Kafka 발행은 Outbox → {@code TokenEventPublisher} 어댑터를 통해 수행한다.
 */
public final class TokenOutputEvents {

	private TokenOutputEvents() {
	}

	public static final String[] ALL = {
			TokenChargedEvent.EVENT_TYPE,
			TokenUsedEvent.EVENT_TYPE,
			TokenRewardedEvent.EVENT_TYPE,
			TokenExpiredEvent.EVENT_TYPE,
			TokenChargeFailedEvent.EVENT_TYPE
	};
}
