package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.GrantTokenUseCase;
import com.planwith.planwith_fo_token.application.service.support.TokenGrantExecutor;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;

@Service
public class GrantTokenService implements GrantTokenUseCase {

	private static final Logger log = LoggerFactory.getLogger(GrantTokenService.class);

	private final TokenGrantExecutor tokenGrantExecutor;

	public GrantTokenService(TokenGrantExecutor tokenGrantExecutor) {
		this.tokenGrantExecutor = tokenGrantExecutor;
	}

	@Override
	@Transactional
	public void grant(GrantTokenCommand command) {
		log.info(
				"GrantTokenService : grant : 토큰 지급 요청 - memberUuid={}, transactionUuid={}, type={}, amount={}",
				command.memberUuid(),
				command.transactionUuid(),
				command.transactionType(),
				command.amount()
		);
		TokenLedger ledger = tokenGrantExecutor.grant(command);
		log.info(
				"GrantTokenService : grant : 토큰 지급 완료 - memberUuid={}, balanceAfter={}, tokenType={}",
				command.memberUuid(),
				ledger.balanceAfter(),
				ledger.tokenType()
		);
	}
}
