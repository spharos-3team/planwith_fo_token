package com.planwith.planwith_fo_token.application.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.ExpireTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.ExpireTokenUseCase;
import com.planwith.planwith_fo_token.application.service.support.TokenLedgerCommandExecutor;

@Service
public class ExpireTokenService implements ExpireTokenUseCase {

	private static final Logger log = LoggerFactory.getLogger(ExpireTokenService.class);

	private final TokenLedgerCommandExecutor commandExecutor;

	public ExpireTokenService(TokenLedgerCommandExecutor commandExecutor) {
		this.commandExecutor = commandExecutor;
	}

	@Override
	@Transactional
	public void expire(ExpireTokenCommand command) {
		log.info("ExpireTokenService : expire : 무료 토큰 만료 요청 - memberUuid={}, transactionUuid={}",
				command.memberUuid(), command.transactionUuid());
		commandExecutor.executeMutation(
				command.transactionUuid(),
				command.memberUuid(),
				wallet -> wallet.expire(command.transactionUuid(), Instant.now())
		);
		log.info("ExpireTokenService : expire : 무료 토큰 만료 완료 - memberUuid={}", command.memberUuid());
	}
}
