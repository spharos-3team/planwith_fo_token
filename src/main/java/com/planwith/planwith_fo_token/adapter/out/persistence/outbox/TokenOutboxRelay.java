package com.planwith.planwith_fo_token.adapter.out.persistence.outbox;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.event.TokenChargeFailedEvent;
import com.planwith.planwith_fo_token.application.event.TokenChargedEvent;
import com.planwith.planwith_fo_token.application.event.TokenExpiredEvent;
import com.planwith.planwith_fo_token.application.event.TokenRewardedEvent;
import com.planwith.planwith_fo_token.application.event.TokenUsedEvent;
import com.planwith.planwith_fo_token.application.port.out.TokenEventPublisher;
import com.planwith.planwith_fo_token.config.TokenKafkaProperties;
import com.planwith.planwith_fo_token.config.TokenOutboxProperties;

@Component
@ConditionalOnProperty(name = "token.outbox.enabled", havingValue = "true")
public class TokenOutboxRelay {

	private static final Logger log = LoggerFactory.getLogger(TokenOutboxRelay.class);

	private final SpringDataTokenOutboxRepository repository;
	private final TokenEventPublisher publisher;
	private final TokenOutboxProperties outboxProperties;
	private final TokenKafkaProperties kafkaProperties;
	private final Clock clock;

	public TokenOutboxRelay(
			SpringDataTokenOutboxRepository repository,
			TokenEventPublisher publisher,
			TokenOutboxProperties outboxProperties,
			TokenKafkaProperties kafkaProperties
	) {
		this(repository, publisher, outboxProperties, kafkaProperties, Clock.systemUTC());
	}

	TokenOutboxRelay(
			SpringDataTokenOutboxRepository repository,
			TokenEventPublisher publisher,
			TokenOutboxProperties outboxProperties,
			TokenKafkaProperties kafkaProperties,
			Clock clock
	) {
		this.repository = repository;
		this.publisher = publisher;
		this.outboxProperties = outboxProperties;
		this.kafkaProperties = kafkaProperties;
		this.clock = clock;
	}

	@Scheduled(
			fixedDelayString = "${token.outbox.relay-interval:5s}",
			initialDelayString = "${token.outbox.relay-initial-delay:5s}"
	)
	@Transactional
	public void relayUnpublishedEvents() {
		int batchSize = outboxProperties.getRelayBatchSize() > 0
				? outboxProperties.getRelayBatchSize()
				: 50;
		Instant now = clock.instant();
		List<TokenOutboxJpaEntity> unpublished = repository.findDueUnpublished(now, PageRequest.of(0, batchSize));
		for (TokenOutboxJpaEntity outbox : unpublished) {
			if (outbox.isDue(now)) {
				publish(outbox, now);
			}
		}
	}

	private void publish(TokenOutboxJpaEntity outbox, Instant now) {
		try {
			publisher.publish(
							topicFor(outbox.eventType()),
							outbox.aggregateUuid().toString(),
							outbox.payload()
					)
					.get(sendTimeoutMillis(), TimeUnit.MILLISECONDS);
			outbox.markPublished(now);
			repository.save(outbox);
			log.info("TokenOutboxRelay : publish : 토큰 Outbox 발행 완료 - eventUuid={}, eventType={}",
					outbox.eventUuid(), outbox.eventType());
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			recordFailure(outbox, now);
			log.warn("TokenOutboxRelay : publish : 토큰 Outbox 발행 중단 - eventUuid={}, retryCount={}",
					outbox.eventUuid(), outbox.retryCount());
		} catch (Exception exception) {
			recordFailure(outbox, now);
			if (outboxProperties.retryLimitReached(outbox.retryCount())) {
				log.error(
						"TokenOutboxRelay : publish : 토큰 Outbox 최대 재시도 이후에도 미발행 유지 - eventUuid={}, retryCount={}",
						outbox.eventUuid(),
						outbox.retryCount()
				);
			} else {
				log.warn("TokenOutboxRelay : publish : 토큰 Outbox 발행 실패 - eventUuid={}, retryCount={}",
						outbox.eventUuid(), outbox.retryCount());
			}
		}
	}

	private void recordFailure(TokenOutboxJpaEntity outbox, Instant now) {
		int nextRetryCount = outbox.retryCount() + 1;
		outbox.recordPublishFailure(outboxProperties.nextRetryAt(now, nextRetryCount));
		repository.save(outbox);
	}

	private String topicFor(String eventType) {
		if (TokenChargedEvent.EVENT_TYPE.equals(eventType)) {
			return kafkaProperties.getTopics().getTokenCharged();
		}
		if (TokenUsedEvent.EVENT_TYPE.equals(eventType)) {
			return kafkaProperties.getTopics().getTokenUsed();
		}
		if (TokenRewardedEvent.EVENT_TYPE.equals(eventType)) {
			return kafkaProperties.getTopics().getTokenRewarded();
		}
		if (TokenExpiredEvent.EVENT_TYPE.equals(eventType)) {
			return kafkaProperties.getTopics().getTokenExpired();
		}
		if (TokenChargeFailedEvent.EVENT_TYPE.equals(eventType)) {
			return kafkaProperties.getTopics().getTokenChargeFailed();
		}
		log.warn("TokenOutboxRelay : topicFor : 알 수 없는 Outbox eventType - eventType={}", eventType);
		return kafkaProperties.getTopics().getTokenCharged();
	}

	private long sendTimeoutMillis() {
		Duration timeout = outboxProperties.getSendTimeout();
		if (timeout == null || timeout.isZero() || timeout.isNegative()) {
			return Duration.ofSeconds(10).toMillis();
		}
		return timeout.toMillis();
	}
}
