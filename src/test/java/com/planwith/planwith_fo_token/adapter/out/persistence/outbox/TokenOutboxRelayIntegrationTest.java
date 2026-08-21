package com.planwith.planwith_fo_token.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.planwith.planwith_fo_token.application.event.TokenChargedEvent;
import com.planwith.planwith_fo_token.application.port.out.TokenEventPublisher;
import com.planwith.planwith_fo_token.config.TokenKafkaProperties;
import com.planwith.planwith_fo_token.config.TokenOutboxProperties;

@SpringBootTest
@ActiveProfiles("test")
class TokenOutboxRelayIntegrationTest {

	@Autowired
	private SpringDataTokenOutboxRepository repository;

	@Autowired
	private TokenOutboxProperties outboxProperties;

	@Autowired
	private TokenKafkaProperties kafkaProperties;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Test
	void relayPublishesToKafkaAndMarksPublishedAt() {
		UUID eventUuid = UUID.fromString("d1111111-1111-1111-1111-111111111111");
		UUID memberUuid = UUID.fromString("d2222222-2222-2222-2222-222222222222");
		Instant now = Instant.parse("2026-08-20T12:00:00Z");
		TokenEventPublisher publisher = mock(TokenEventPublisher.class);

		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.executeWithoutResult(status -> repository.save(new TokenOutboxJpaEntity(
				eventUuid,
				TokenChargedEvent.AGGREGATE_TYPE,
				memberUuid,
				TokenChargedEvent.EVENT_TYPE,
				"{\"amount\":10}",
				now
		)));

		when(publisher.publish(
				eq(kafkaProperties.getTopics().getTokenCharged()),
				eq(memberUuid.toString()),
				anyString()
		)).thenReturn(CompletableFuture.completedFuture(null));

		TokenOutboxRelay relay = new TokenOutboxRelay(
				repository,
				publisher,
				outboxProperties,
				kafkaProperties,
				fixedClockProvider(now)
		);
		relay.relayUnpublishedEvents();

		TokenOutboxJpaEntity published = repository.findByEventUuid(eventUuid).orElseThrow();
		assertThat(published.publishedAt()).isEqualTo(now);
		assertThat(published.retryCount()).isZero();
	}

	@Test
	void relayKeepsOutboxUnpublishedWhenKafkaFails() {
		UUID eventUuid = UUID.fromString("d3333333-3333-3333-3333-333333333333");
		UUID memberUuid = UUID.fromString("d4444444-4444-4444-4444-444444444444");
		Instant now = Instant.parse("2026-08-20T12:00:00Z");
		TokenEventPublisher publisher = mock(TokenEventPublisher.class);

		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.executeWithoutResult(status -> repository.save(new TokenOutboxJpaEntity(
				eventUuid,
				TokenChargedEvent.AGGREGATE_TYPE,
				memberUuid,
				TokenChargedEvent.EVENT_TYPE,
				"{\"amount\":10}",
				now
		)));

		when(publisher.publish(
				eq(kafkaProperties.getTopics().getTokenCharged()),
				eq(memberUuid.toString()),
				anyString()
		)).thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));

		TokenOutboxRelay relay = new TokenOutboxRelay(
				repository,
				publisher,
				outboxProperties,
				kafkaProperties,
				fixedClockProvider(now)
		);
		relay.relayUnpublishedEvents();

		TokenOutboxJpaEntity unpublished = repository.findByEventUuid(eventUuid).orElseThrow();
		assertThat(unpublished.publishedAt()).isNull();
		assertThat(unpublished.retryCount()).isEqualTo(1);
		assertThat(unpublished.nextRetryAt()).isAfter(now);
	}

	@SuppressWarnings("unchecked")
	private static ObjectProvider<Clock> fixedClockProvider(Instant now) {
		ObjectProvider<Clock> clockProvider = mock(ObjectProvider.class);
		when(clockProvider.getIfAvailable()).thenReturn(Clock.fixed(now, ZoneOffset.UTC));
		return clockProvider;
	}
}
