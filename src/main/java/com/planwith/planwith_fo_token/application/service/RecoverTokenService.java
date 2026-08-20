package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;
import com.planwith.planwith_fo_token.application.command.RecoverTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.GrantTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.RecoverTokenUseCase;

@Service
public class RecoverTokenService implements RecoverTokenUseCase {

	private static final Logger log = LoggerFactory.getLogger(RecoverTokenService.class);

	private final GrantTokenUseCase grantTokenUseCase;

	public RecoverTokenService(GrantTokenUseCase grantTokenUseCase) {
		this.grantTokenUseCase = grantTokenUseCase;
	}

	@Override
	@Transactional
	public void recover(RecoverTokenCommand command) {
		log.info("RecoverTokenService : recover : 충전 실패 복구 지급 위임 - memberUuid={}, transactionUuid={}",
				command.memberUuid(), command.transactionUuid());
		grantTokenUseCase.grant(GrantTokenCommand.fromRecover(command));
		log.info("RecoverTokenService : recover : 충전 실패 복구 지급 완료 - memberUuid={}", command.memberUuid());
	}
}
