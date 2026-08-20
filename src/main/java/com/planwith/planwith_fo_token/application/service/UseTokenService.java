package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.UseTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.UseTokenUseCase;
import com.planwith.planwith_fo_token.application.service.support.TokenUseExecutor;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;

@Service
public class UseTokenService implements UseTokenUseCase {

	private static final Logger log = LoggerFactory.getLogger(UseTokenService.class);

	private final TokenUseExecutor tokenUseExecutor;

	public UseTokenService(TokenUseExecutor tokenUseExecutor) {
		this.tokenUseExecutor = tokenUseExecutor;
	}

	@Override
	@Transactional
	public void use(UseTokenCommand command) {
		log.info(
				"UseTokenService : use : 토큰 사용 요청 - memberUuid={}, transactionUuid={}, amount={}, referenceType={}",
				command.memberUuid(),
				command.transactionUuid(),
				command.amount(),
				command.referenceType()
		);
		TokenLedger ledger = tokenUseExecutor.use(command);
		log.info(
				"UseTokenService : use : 토큰 사용 완료 - memberUuid={}, amount={}, balanceAfter={}",
				command.memberUuid(),
				ledger.amount(),
				ledger.balanceAfter()
		);
	}
}
