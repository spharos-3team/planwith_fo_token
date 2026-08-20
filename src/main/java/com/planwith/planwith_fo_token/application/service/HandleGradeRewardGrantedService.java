package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.HandleGradeRewardGrantedCommand;
import com.planwith.planwith_fo_token.application.port.in.HandleGradeRewardGrantedUseCase;
import com.planwith.planwith_fo_token.application.port.out.ProcessedTokenEventPort;

@Service
public class HandleGradeRewardGrantedService implements HandleGradeRewardGrantedUseCase {

	private static final Logger log = LoggerFactory.getLogger(HandleGradeRewardGrantedService.class);

	private final ProcessedTokenEventPort processedTokenEventPort;

	public HandleGradeRewardGrantedService(ProcessedTokenEventPort processedTokenEventPort) {
		this.processedTokenEventPort = processedTokenEventPort;
	}

	@Override
	@Transactional
	public void handle(HandleGradeRewardGrantedCommand command) {
		if (processedTokenEventPort.existsByEventUuid(command.eventUuid())) {
			log.warn("HandleGradeRewardGrantedService : handle : 중복 GradeRewardGranted 이벤트 무시 - eventUuid={}",
					command.eventUuid());
			return;
		}
		log.info("HandleGradeRewardGrantedService : handle : GradeRewardGranted 이벤트 수신 (보상 정책 미구현) - eventUuid={}, memberUuid={}",
				command.eventUuid(), command.memberUuid());
	}
}
