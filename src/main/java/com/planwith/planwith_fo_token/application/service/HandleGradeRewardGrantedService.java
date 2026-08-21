package com.planwith.planwith_fo_token.application.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.ExpireTokenCommand;
import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;
import com.planwith.planwith_fo_token.application.command.HandleGradeRewardGrantedCommand;
import com.planwith.planwith_fo_token.application.event.TokenRewardedEvent;
import com.planwith.planwith_fo_token.application.port.in.HandleGradeRewardGrantedUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.ExpireTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.GrantTokenUseCase;
import com.planwith.planwith_fo_token.application.port.out.GradeMonthlyTokenGrantPort;
import com.planwith.planwith_fo_token.application.port.out.ProcessedTokenEventPort;
import com.planwith.planwith_fo_token.domain.model.GradeMonthlyTokenGrant;
import com.planwith.planwith_fo_token.domain.model.ProcessedTokenEvent;
import com.planwith.planwith_fo_token.domain.model.TokenType;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;
import com.planwith.planwith_fo_token.domain.service.TokenPolicy;

@Service
public class HandleGradeRewardGrantedService implements HandleGradeRewardGrantedUseCase {

	private static final Logger log = LoggerFactory.getLogger(HandleGradeRewardGrantedService.class);
	private static final String MONTHLY_FREE_TOKEN = "MONTHLY_FREE_TOKEN";

	private final ProcessedTokenEventPort processedTokenEventPort;
	private final GradeMonthlyTokenGrantPort gradeMonthlyTokenGrantPort;
	private final ExpireTokenUseCase expireTokenUseCase;
	private final GrantTokenUseCase grantTokenUseCase;

	public HandleGradeRewardGrantedService(
			ProcessedTokenEventPort processedTokenEventPort,
			GradeMonthlyTokenGrantPort gradeMonthlyTokenGrantPort,
			ExpireTokenUseCase expireTokenUseCase,
			GrantTokenUseCase grantTokenUseCase
	) {
		this.processedTokenEventPort = processedTokenEventPort;
		this.gradeMonthlyTokenGrantPort = gradeMonthlyTokenGrantPort;
		this.expireTokenUseCase = expireTokenUseCase;
		this.grantTokenUseCase = grantTokenUseCase;
	}

	@Override
	@Transactional
	public void handle(HandleGradeRewardGrantedCommand command) {
		if (processedTokenEventPort.existsByEventUuid(command.eventUuid())) {
			log.warn(
					"HandleGradeRewardGrantedService : handle : 중복 GradeRewardGranted 이벤트 무시 - eventUuid={}",
					command.eventUuid()
			);
			return;
		}

		String rewardMonth = GradeMonthlyTokenGrant.requireRewardMonth(command.rewardMonth());
		if (gradeMonthlyTokenGrantPort.existsByMemberUuidAndRewardMonth(command.memberUuid(), rewardMonth)) {
			log.warn(
					"HandleGradeRewardGrantedService : handle : 동일 회원·월 등급 토큰 이미 지급됨 - memberUuid={}, rewardMonth={}, eventUuid={}",
					command.memberUuid(),
					rewardMonth,
					command.eventUuid()
			);
			recordProcessedEvent(command);
			return;
		}

		if (command.tokenAmount() <= 0) {
			throw new IllegalArgumentException(
					"Grade reward tokenAmount must be positive. eventUuid=" + command.eventUuid()
			);
		}
		if (command.rewardType() != null
				&& !command.rewardType().isBlank()
				&& !MONTHLY_FREE_TOKEN.equalsIgnoreCase(command.rewardType().trim())) {
			log.warn(
					"HandleGradeRewardGrantedService : handle : 예상과 다른 rewardType - eventUuid={}, rewardType={}",
					command.eventUuid(),
					command.rewardType()
			);
		}

		TransactionUuid expireTransactionUuid = GradeMonthlyTokenGrant.expireLedgerTransactionUuidOf(
				command.memberUuid(),
				rewardMonth
		);
		TransactionUuid grantTransactionUuid = GradeMonthlyTokenGrant.ledgerTransactionUuidOf(
				command.memberUuid(),
				rewardMonth
		);
		Instant grantedAt = command.grantedAt() == null ? Instant.now() : command.grantedAt();

		if (TokenPolicy.expiresBeforeMonthlyGrant(TokenType.FREE)) {
			log.info(
					"HandleGradeRewardGrantedService : handle : 월간 FREE 만료 후 등급 토큰 지급 시작 - eventUuid={}, memberUuid={}, rewardMonth={}",
					command.eventUuid(),
					command.memberUuid(),
					rewardMonth
			);
			expireTokenUseCase.expire(new ExpireTokenCommand(expireTransactionUuid, command.memberUuid()));
		}

		log.info(
				"HandleGradeRewardGrantedService : handle : 등급 무료 토큰 지급 시작 - eventUuid={}, memberUuid={}, rewardMonth={}, gradeCode={}, tokenAmount={}",
				command.eventUuid(),
				command.memberUuid(),
				rewardMonth,
				command.gradeCode(),
				command.tokenAmount()
		);

		grantTokenUseCase.grant(GrantTokenCommand.gradeReward(
				grantTransactionUuid,
				command.memberUuid(),
				command.tokenAmount(),
				rewardMonth,
				"Grade monthly free token " + rewardMonth
						+ (command.gradeCode() == null ? "" : " " + command.gradeCode())
		));

		boolean monthlySaved = gradeMonthlyTokenGrantPort.saveIdempotent(GradeMonthlyTokenGrant.recorded(
				command.memberUuid(),
				rewardMonth,
				command.eventUuid(),
				command.tokenAmount(),
				command.gradeCode(),
				grantedAt
		));
		if (!monthlySaved) {
			log.warn(
					"HandleGradeRewardGrantedService : handle : 회원·월 지급 기록 경합 - memberUuid={}, rewardMonth={}, eventUuid={}",
					command.memberUuid(),
					rewardMonth,
					command.eventUuid()
			);
		}

		recordProcessedEvent(command);
		log.info(
				"HandleGradeRewardGrantedService : handle : 등급 무료 토큰 지급 완료 - eventUuid={}, memberUuid={}, rewardMonth={}, expireTransactionUuid={}, grantTransactionUuid={}",
				command.eventUuid(),
				command.memberUuid(),
				rewardMonth,
				expireTransactionUuid,
				grantTransactionUuid
		);
	}

	private void recordProcessedEvent(HandleGradeRewardGrantedCommand command) {
		Instant processedAt = command.grantedAt() == null ? Instant.now() : command.grantedAt();
		boolean recorded = processedTokenEventPort.saveIdempotent(ProcessedTokenEvent.recorded(
				command.eventUuid(),
				command.memberUuid(),
				TokenRewardedEvent.EVENT_TYPE,
				processedAt
		));
		if (!recorded) {
			log.warn(
					"HandleGradeRewardGrantedService : handle : GradeRewardGranted 처리 기록 경합 - eventUuid={}",
					command.eventUuid()
			);
		}
	}
}
