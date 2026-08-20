package com.planwith.planwith_fo_token.adapter.in.kafka;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_token.adapter.in.kafka.dto.PaymentCompletedInboundEvent;
import com.planwith.planwith_fo_token.application.command.HandlePaymentCompletedCommand;
import com.planwith.planwith_fo_token.application.port.in.HandlePaymentCompletedUseCase;
import com.planwith.planwith_fo_token.config.TokenKafkaProperties;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@Component
@ConditionalOnProperty(name = "token.kafka.consumer-enabled", havingValue = "true")
public class PaymentCompletedEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(PaymentCompletedEventConsumer.class);

	private final ObjectMapper objectMapper;
	private final HandlePaymentCompletedUseCase handlePaymentCompletedUseCase;
	private final TokenKafkaProperties kafkaProperties;

	public PaymentCompletedEventConsumer(
			ObjectMapper objectMapper,
			HandlePaymentCompletedUseCase handlePaymentCompletedUseCase,
			TokenKafkaProperties kafkaProperties
	) {
		this.objectMapper = objectMapper;
		this.handlePaymentCompletedUseCase = handlePaymentCompletedUseCase;
		this.kafkaProperties = kafkaProperties;
	}

	@KafkaListener(topics = "${token.kafka.topics.payment-completed}")
	public void consume(String payload) {
		try {
			PaymentCompletedInboundEvent event = objectMapper.readValue(payload, PaymentCompletedInboundEvent.class);
			if (event.eventUuid() == null || event.memberUuid() == null) {
				throw new IllegalArgumentException("PaymentCompleted payload is invalid.");
			}
			log.info("PaymentCompletedEventConsumer : consume : PaymentCompleted 이벤트 수신 - eventUuid={}",
					event.eventUuid());
			handlePaymentCompletedUseCase.handle(new HandlePaymentCompletedCommand(
					UUID.fromString(event.eventUuid()),
					MemberUuid.from(event.memberUuid()),
					event.tokenAmount() != null ? event.tokenAmount() : 0L,
					event.paymentReference(),
					event.completedAt()
			));
		} catch (JsonProcessingException | IllegalArgumentException exception) {
			log.warn("PaymentCompletedEventConsumer : consume : PaymentCompleted 파싱 실패 - payload={}",
					payload);
		} catch (RuntimeException exception) {
			log.warn("PaymentCompletedEventConsumer : consume : PaymentCompleted 처리 실패 - topic={}",
					kafkaProperties.getTopics().getPaymentCompleted());
			throw exception;
		}
	}
}
