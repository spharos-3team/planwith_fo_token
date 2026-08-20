package com.planwith.planwith_fo_token.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.IllegalTransactionStateException;

import com.planwith.planwith_fo_token.application.event.TokenRewardedEvent;
import com.planwith.planwith_fo_token.application.port.out.TokenEventOutboxPort;
import com.planwith.planwith_fo_token.application.port.out.TokenOutboxMessage;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TokenOutboxAdapterIntegrationTest {

	@Autowired
	private TokenEventOutboxPort tokenEventOutboxPort;

	@Autowired
	private SpringDataTokenOutboxRepository repository;

	@Test
	void saveOutboxMessageWithinActiveTransaction() {
		Instant occurredAt = Instant.parse("2026-08-20T00:00:00Z");
		tokenEventOutboxPort.save(new TokenOutboxMessage(
				"33333333-3333-3333-3333-333333333333",
				TokenRewardedEvent.AGGREGATE_TYPE,
				"22222222-2222-2222-2222-222222222222",
				TokenRewardedEvent.EVENT_TYPE,
				"{\"tokenAmount\":100}",
				occurredAt
		));

		assertThat(repository.existsByEventUuid(
				UUID.fromString("33333333-3333-3333-3333-333333333333")
		)).isTrue();
		TokenOutboxJpaEntity saved = repository.findByEventUuid(
				UUID.fromString("33333333-3333-3333-3333-333333333333")
		).orElseThrow();
		assertThat(saved.occurredAt()).isEqualTo(occurredAt);
		assertThat(saved.publishedAt()).isNull();
		assertThat(saved.retryCount()).isZero();
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void saveRequiresActiveTransaction() {
		assertThatThrownBy(() -> tokenEventOutboxPort.save(new TokenOutboxMessage(
				"44444444-4444-4444-4444-444444444444",
				TokenRewardedEvent.AGGREGATE_TYPE,
				"22222222-2222-2222-2222-222222222222",
				TokenRewardedEvent.EVENT_TYPE,
				"{}",
				Instant.now()
		))).isInstanceOf(IllegalTransactionStateException.class);
	}
}
