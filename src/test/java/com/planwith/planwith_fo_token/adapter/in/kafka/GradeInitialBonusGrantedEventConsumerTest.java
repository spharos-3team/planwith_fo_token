package com.planwith.planwith_fo_token.adapter.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_token.application.command.HandleGradeInitialBonusGrantedCommand;
import com.planwith.planwith_fo_token.application.port.in.HandleGradeInitialBonusGrantedUseCase;
import com.planwith.planwith_fo_token.config.TokenKafkaProperties;

class GradeInitialBonusGrantedEventConsumerTest {

	@Test
	void consumesMinimalInitialBonusEventContract() {
		HandleGradeInitialBonusGrantedUseCase useCase = mock(HandleGradeInitialBonusGrantedUseCase.class);
		GradeInitialBonusGrantedEventConsumer consumer = new GradeInitialBonusGrantedEventConsumer(
				new ObjectMapper(),
				useCase,
				new TokenKafkaProperties()
		);

		consumer.consume("""
				{
				  "eventUuid": "f4444444-4444-4444-4444-444444444444",
				  "memberUuid": "f1818181-1818-1818-1818-181818181818",
				  "tokenAmount": 25
				}
				""");

		ArgumentCaptor<HandleGradeInitialBonusGrantedCommand> captor =
				ArgumentCaptor.forClass(HandleGradeInitialBonusGrantedCommand.class);
		verify(useCase).handle(captor.capture());
		assertThat(captor.getValue().tokenAmount()).isEqualTo(25L);
		assertThat(captor.getValue().gradeCode()).isNull();
		assertThat(captor.getValue().grantedAt()).isNull();
	}
}
