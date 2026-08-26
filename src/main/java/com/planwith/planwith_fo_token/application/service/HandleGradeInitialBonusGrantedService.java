package com.planwith.planwith_fo_token.application.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;
import com.planwith.planwith_fo_token.application.command.HandleGradeInitialBonusGrantedCommand;
import com.planwith.planwith_fo_token.application.port.in.HandleGradeInitialBonusGrantedUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.GrantTokenUseCase;
import com.planwith.planwith_fo_token.application.port.out.ProcessedTokenEventPort;
import com.planwith.planwith_fo_token.domain.model.ProcessedTokenEvent;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

@Service
public class HandleGradeInitialBonusGrantedService implements HandleGradeInitialBonusGrantedUseCase {

	private static final Logger log = LoggerFactory.getLogger(HandleGradeInitialBonusGrantedService.class);
	private static final String EVENT_TYPE = "GradeInitialBonusGranted";
	private static final String IDEMPOTENCY_KEY_PREFIX = "GRADE_INITIAL_BONUS:";

	private final ProcessedTokenEventPort processedTokenEventPort;
	private final GrantTokenUseCase grantTokenUseCase;

	public HandleGradeInitialBonusGrantedService(
			ProcessedTokenEventPort processedTokenEventPort,
			GrantTokenUseCase grantTokenUseCase
	) {
		this.processedTokenEventPort = processedTokenEventPort;
		this.grantTokenUseCase = grantTokenUseCase;
	}

	@Override
	@Transactional
	public void handle(HandleGradeInitialBonusGrantedCommand command) {
		validate(command);
		if (processedTokenEventPort.existsByEventUuid(command.eventUuid())) {
			log.warn(
					"HandleGradeInitialBonusGrantedService : handle : 중복 최초 등급 BONUS 이벤트 무시 - eventUuid={}",
					command.eventUuid()
			);
			return;
		}

		TransactionUuid transactionUuid = initialBonusTransactionUuid(command);
		log.info(
				"HandleGradeInitialBonusGrantedService : handle : 최초 등급 BONUS 토큰 지급 시작 - eventUuid={}, memberUuid={}, gradeCode={}, tokenAmount={}",
				command.eventUuid(),
				command.memberUuid(),
				command.gradeCode(),
				command.tokenAmount()
		);
		grantTokenUseCase.grant(GrantTokenCommand.bonusReward(
				transactionUuid,
				command.memberUuid(),
				command.tokenAmount(),
				command.eventUuid().toString(),
				"Initial grade bonus" + gradeDescription(command.gradeCode())
		));

		Instant processedAt = command.grantedAt() == null ? Instant.now() : command.grantedAt();
		boolean recorded = processedTokenEventPort.saveIdempotent(ProcessedTokenEvent.recorded(
				command.eventUuid(),
				command.memberUuid(),
				EVENT_TYPE,
				processedAt
		));
		if (!recorded) {
			log.warn(
					"HandleGradeInitialBonusGrantedService : handle : 최초 등급 BONUS 처리 기록 경합 - eventUuid={}",
					command.eventUuid()
			);
		}
		log.info(
				"HandleGradeInitialBonusGrantedService : handle : 최초 등급 BONUS 토큰 지급 처리 완료 - eventUuid={}, memberUuid={}, transactionUuid={}",
				command.eventUuid(),
				command.memberUuid(),
				transactionUuid
		);
	}

	private static void validate(HandleGradeInitialBonusGrantedCommand command) {
		if (command == null || command.eventUuid() == null || command.memberUuid() == null) {
			throw new IllegalArgumentException("Grade initial bonus command identifiers are required.");
		}
		if (command.tokenAmount() <= 0) {
			throw new IllegalArgumentException("Grade initial bonus tokenAmount must be positive.");
		}
	}

	private static TransactionUuid initialBonusTransactionUuid(HandleGradeInitialBonusGrantedCommand command) {
		String key = IDEMPOTENCY_KEY_PREFIX + command.memberUuid().value();
		return new TransactionUuid(UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)));
	}

	private static String gradeDescription(String gradeCode) {
		if (gradeCode == null || gradeCode.isBlank()) {
			return "";
		}
		return " " + gradeCode.trim();
	}
}
