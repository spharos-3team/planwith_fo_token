package com.planwith.planwith_fo_token.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.planwith.planwith_fo_token.application.event.TokenChargeFailedEvent;
import com.planwith.planwith_fo_token.application.event.TokenChargedEvent;
import com.planwith.planwith_fo_token.application.event.TokenExpiredEvent;
import com.planwith.planwith_fo_token.application.event.TokenRewardedEvent;
import com.planwith.planwith_fo_token.application.event.TokenUsedEvent;
import com.planwith.planwith_fo_token.application.port.out.TokenEventPublisher;
import com.planwith.planwith_fo_token.config.TokenKafkaProperties;
import com.planwith.planwith_fo_token.config.TokenOutboxProperties;

@SpringBootTest
@ActiveProfiles("test")
class TokenOutboxRelayTopicMappingIntegrationTest {

	@Autowired
	private SpringDataTokenOutboxRepository repository;

	@Autowired
	private TokenOutboxProperties outboxProperties;

	@Autowired
	private TokenKafkaProperties kafkaProperties;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@ParameterizedTest
	@MethodSource("outputEvents")
	void relayPublishesEachOutputEventToConfiguredTopic(String eventType, String expectedTopic) {
		UUID eventUuid = UUID.randomUUID();
		UUID aggregateUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-21T02:00:00Z");
		TokenEventPublisher publisher = mock(TokenEventPublisher.class);

		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.executeWithoutResult(status -> repository.save(new TokenOutboxJpaEntity(
				eventUuid,
				"TokenWallet",
				aggregateUuid,
				eventType,
				"{\"eventType\":\"" + eventType + "\"}",
				now
		)));

		when(publisher.publish(eq(expectedTopic), eq(aggregateUuid.toString()), anyString()))
				.thenReturn(CompletableFuture.completedFuture(null));

		TokenOutboxRelay relay = new TokenOutboxRelay(
				repository,
				publisher,
				outboxProperties,
				kafkaProperties,
				fixedClockProvider(now)
		);
		relay.relayUnpublishedEvents();

		verify(publisher).publish(eq(expectedTopic), eq(aggregateUuid.toString()), anyString());
		assertThat(repository.findByEventUuid(eventUuid).orElseThrow().publishedAt()).isEqualTo(now);
	}

	@SuppressWarnings("unchecked")
	private static ObjectProvider<Clock> fixedClockProvider(Instant now) {
		ObjectProvider<Clock> clockProvider = mock(ObjectProvider.class);
		when(clockProvider.getIfAvailable()).thenReturn(Clock.fixed(now, ZoneOffset.UTC));
		return clockProvider;
	}

	static Stream<Arguments> outputEvents() {
		return Stream.of(
				Arguments.of(TokenChargedEvent.EVENT_TYPE, "planwith.token.charged"),
				Arguments.of(TokenUsedEvent.EVENT_TYPE, "planwith.token.used"),
				Arguments.of(TokenRewardedEvent.EVENT_TYPE, "planwith.token.rewarded"),
				Arguments.of(TokenExpiredEvent.EVENT_TYPE, "planwith.token.expired"),
				Arguments.of(TokenChargeFailedEvent.EVENT_TYPE, "planwith.token.charge-failed")
		);
	}
}
