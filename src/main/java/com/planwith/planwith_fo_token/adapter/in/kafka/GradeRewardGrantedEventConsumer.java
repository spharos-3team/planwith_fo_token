package com.planwith.planwith_fo_token.adapter.in.kafka;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_token.adapter.in.kafka.dto.GradeRewardGrantedInboundEvent;
import com.planwith.planwith_fo_token.application.command.HandleGradeRewardGrantedCommand;
import com.planwith.planwith_fo_token.application.port.in.HandleGradeRewardGrantedUseCase;
import com.planwith.planwith_fo_token.config.TokenKafkaProperties;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@Component
@ConditionalOnProperty(name = "token.kafka.consumer-enabled", havingValue = "true")
public class GradeRewardGrantedEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(GradeRewardGrantedEventConsumer.class);

	private final ObjectMapper objectMapper;
	private final HandleGradeRewardGrantedUseCase handleGradeRewardGrantedUseCase;
	private final TokenKafkaProperties kafkaProperties;

	public GradeRewardGrantedEventConsumer(
			ObjectMapper objectMapper,
			HandleGradeRewardGrantedUseCase handleGradeRewardGrantedUseCase,
			TokenKafkaProperties kafkaProperties
	) {
		this.objectMapper = objectMapper;
		this.handleGradeRewardGrantedUseCase = handleGradeRewardGrantedUseCase;
		this.kafkaProperties = kafkaProperties;
	}

	@KafkaListener(topics = "${token.kafka.topics.grade-reward-granted}")
	public void consume(String payload) {
		try {
			GradeRewardGrantedInboundEvent event = objectMapper.readValue(payload, GradeRewardGrantedInboundEvent.class);
			if (event.eventUuid() == null || event.memberUuid() == null) {
				throw new IllegalArgumentException("GradeRewardGranted payload is invalid.");
			}
			log.info(
					"GradeRewardGrantedEventConsumer : consume : GradeRewardGranted 이벤트 수신 - eventUuid={}, memberUuid={}, rewardMonth={}, gradeCode={}",
					event.eventUuid(),
					event.memberUuid(),
					event.rewardMonth(),
					event.gradeCode()
			);
			handleGradeRewardGrantedUseCase.handle(new HandleGradeRewardGrantedCommand(
					UUID.fromString(event.eventUuid()),
					MemberUuid.from(event.memberUuid()),
					event.tokenAmount() != null ? event.tokenAmount() : 0L,
					event.rewardType(),
					event.rewardMonth(),
					event.gradeCode(),
					event.grantedAt()
			));
		} catch (JsonProcessingException | IllegalArgumentException exception) {
			log.warn("GradeRewardGrantedEventConsumer : consume : GradeRewardGranted 파싱 실패 - payload={}",
					payload);
		} catch (RuntimeException exception) {
			log.warn("GradeRewardGrantedEventConsumer : consume : GradeRewardGranted 처리 실패 - topic={}",
					kafkaProperties.getTopics().getGradeRewardGranted());
			throw exception;
		}
	}
}
