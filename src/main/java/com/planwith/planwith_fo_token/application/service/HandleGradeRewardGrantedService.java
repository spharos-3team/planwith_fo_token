package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;
import com.planwith.planwith_fo_token.application.command.HandleGradeRewardGrantedCommand;
import com.planwith.planwith_fo_token.application.event.TokenRewardedEvent;
import com.planwith.planwith_fo_token.application.port.in.HandleGradeRewardGrantedUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.GrantTokenUseCase;
import com.planwith.planwith_fo_token.application.port.out.ProcessedTokenEventPort;
import com.planwith.planwith_fo_token.domain.model.ProcessedTokenEvent;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

@Service
public class HandleGradeRewardGrantedService implements HandleGradeRewardGrantedUseCase {

	private static final Logger log = LoggerFactory.getLogger(HandleGradeRewardGrantedService.class);

	private final ProcessedTokenEventPort processedTokenEventPort;
	private final GrantTokenUseCase grantTokenUseCase;

	public HandleGradeRewardGrantedService(
			ProcessedTokenEventPort processedTokenEventPort,
			GrantTokenUseCase grantTokenUseCase
	) {
		this.processedTokenEventPort = processedTokenEventPort;
		this.grantTokenUseCase = grantTokenUseCase;
	}

	@Override
	@Transactional
	public void handle(HandleGradeRewardGrantedCommand command) {
		if (processedTokenEventPort.existsByEventUuid(command.eventUuid())) {
			log.warn("HandleGradeRewardGrantedService : handle : 중복 GradeRewardGranted 이벤트 무시 - eventUuid={}",
					command.eventUuid());
			return;
		}
		log.info(
				"HandleGradeRewardGrantedService : handle : 등급 무료 토큰 지급 시작 - eventUuid={}, memberUuid={}",
				command.eventUuid(),
				command.memberUuid()
		);
		grantTokenUseCase.grant(GrantTokenCommand.gradeReward(
				new TransactionUuid(command.eventUuid()),
				command.memberUuid(),
				command.tokenAmount(),
				command.rewardType(),
				"Grade reward token grant"
		));
		boolean recorded = processedTokenEventPort.saveIdempotent(ProcessedTokenEvent.recorded(
				command.eventUuid(),
				command.memberUuid(),
				TokenRewardedEvent.EVENT_TYPE,
				command.grantedAt()
		));
		if (!recorded) {
			log.warn("HandleGradeRewardGrantedService : handle : GradeRewardGranted 처리 기록 경합 - eventUuid={}",
					command.eventUuid());
		}
		log.info("HandleGradeRewardGrantedService : handle : 등급 무료 토큰 지급 완료 - eventUuid={}",
				command.eventUuid());
	}
}
