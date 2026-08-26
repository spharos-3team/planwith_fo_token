package com.planwith.planwith_fo_token.adapter.in.kafka;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_token.adapter.in.kafka.dto.GradeInitialBonusGrantedInboundEvent;
import com.planwith.planwith_fo_token.application.command.HandleGradeInitialBonusGrantedCommand;
import com.planwith.planwith_fo_token.application.port.in.HandleGradeInitialBonusGrantedUseCase;
import com.planwith.planwith_fo_token.config.TokenKafkaProperties;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@Component
@ConditionalOnProperty(name = "token.kafka.consumer-enabled", havingValue = "true")
public class GradeInitialBonusGrantedEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(GradeInitialBonusGrantedEventConsumer.class);

	private final ObjectMapper objectMapper;
	private final HandleGradeInitialBonusGrantedUseCase handleGradeInitialBonusGrantedUseCase;
	private final TokenKafkaProperties kafkaProperties;

	public GradeInitialBonusGrantedEventConsumer(
			ObjectMapper objectMapper,
			HandleGradeInitialBonusGrantedUseCase handleGradeInitialBonusGrantedUseCase,
			TokenKafkaProperties kafkaProperties
	) {
		this.objectMapper = objectMapper;
		this.handleGradeInitialBonusGrantedUseCase = handleGradeInitialBonusGrantedUseCase;
		this.kafkaProperties = kafkaProperties;
	}

	@KafkaListener(topics = "${token.kafka.topics.grade-initial-bonus-granted}")
	public void consume(String payload) {
		HandleGradeInitialBonusGrantedCommand command = toCommand(payload);
		log.info(
				"GradeInitialBonusGrantedEventConsumer : consume : 최초 등급 BONUS 이벤트 수신 - eventUuid={}, memberUuid={}, gradeCode={}",
				command.eventUuid(),
				command.memberUuid(),
				command.gradeCode()
		);
		try {
			handleGradeInitialBonusGrantedUseCase.handle(command);
		} catch (RuntimeException exception) {
			log.warn(
					"GradeInitialBonusGrantedEventConsumer : consume : 최초 등급 BONUS 처리 실패 - topic={}, eventUuid={}",
					kafkaProperties.getTopics().getGradeInitialBonusGranted(),
					command.eventUuid()
			);
			throw exception;
		}
	}

	private HandleGradeInitialBonusGrantedCommand toCommand(String payload) {
		try {
			GradeInitialBonusGrantedInboundEvent event = objectMapper.readValue(
					payload,
					GradeInitialBonusGrantedInboundEvent.class
			);
			if (event.eventUuid() == null || event.memberUuid() == null || event.tokenAmount() == null) {
				throw new IllegalArgumentException("GradeInitialBonusGranted payload is invalid.");
			}
			return new HandleGradeInitialBonusGrantedCommand(
					UUID.fromString(event.eventUuid()),
					MemberUuid.from(event.memberUuid()),
					event.tokenAmount(),
					event.gradeCode(),
					event.grantedAt()
			);
		} catch (JsonProcessingException exception) {
			log.warn(
					"GradeInitialBonusGrantedEventConsumer : toCommand : 최초 등급 BONUS 이벤트 JSON 파싱 실패"
			);
			throw new IllegalArgumentException("GradeInitialBonusGranted payload JSON is invalid.", exception);
		} catch (IllegalArgumentException exception) {
			log.warn(
					"GradeInitialBonusGrantedEventConsumer : toCommand : 최초 등급 BONUS 이벤트 필드 검증 실패"
			);
			throw exception;
		}
	}
}
