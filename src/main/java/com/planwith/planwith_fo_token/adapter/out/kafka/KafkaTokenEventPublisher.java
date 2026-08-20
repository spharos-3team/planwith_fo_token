package com.planwith.planwith_fo_token.adapter.out.kafka;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_token.application.port.out.TokenEventPublisher;

@Component
public class KafkaTokenEventPublisher implements TokenEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(KafkaTokenEventPublisher.class);

	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaTokenEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public CompletableFuture<Void> publish(String topic, String key, String payload) {
		log.info("KafkaTokenEventPublisher : publish : 토큰 이벤트 Kafka 발행 시작 - topic={}, key={}",
				topic, key);
		return kafkaTemplate.send(topic, key, payload)
				.thenAccept(result -> log.info(
						"KafkaTokenEventPublisher : publish : 토큰 이벤트 Kafka 발행 완료 - topic={}, key={}",
						topic, key
				));
	}
}
