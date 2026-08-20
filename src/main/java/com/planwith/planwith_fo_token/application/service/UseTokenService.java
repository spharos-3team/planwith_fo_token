package com.planwith.planwith_fo_token.application.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.UseTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.UseTokenUseCase;
import com.planwith.planwith_fo_token.application.service.support.TokenCommandSupport;
import com.planwith.planwith_fo_token.application.service.support.TokenLedgerCommandExecutor;

@Service
public class UseTokenService implements UseTokenUseCase {

	private static final Logger log = LoggerFactory.getLogger(UseTokenService.class);

	private final TokenLedgerCommandExecutor commandExecutor;

	public UseTokenService(TokenLedgerCommandExecutor commandExecutor) {
		this.commandExecutor = commandExecutor;
	}

	@Override
	@Transactional
	public void use(UseTokenCommand command) {
		log.info("UseTokenService : use : 토큰 사용 요청 - memberUuid={}, transactionUuid={}",
				command.memberUuid(), command.transactionUuid());
		commandExecutor.executeMutation(
				command.transactionUuid(),
				command.memberUuid(),
				wallet -> wallet.use(
						command.transactionUuid(),
						command.amount(),
						TokenCommandSupport.parseReferenceType(command.referenceType()),
						TokenCommandSupport.descriptionOrDefault(command.description(), "Token use"),
						Instant.now()
				)
		);
		log.info("UseTokenService : use : 토큰 사용 완료 - memberUuid={}", command.memberUuid());
	}
}
