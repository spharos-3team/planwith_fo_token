package com.planwith.planwith_fo_token.application.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.GrantTokenUseCase;
import com.planwith.planwith_fo_token.application.service.support.TokenCommandSupport;
import com.planwith.planwith_fo_token.application.service.support.TokenLedgerCommandExecutor;
import com.planwith.planwith_fo_token.domain.model.TransactionType;

@Service
public class GrantTokenService implements GrantTokenUseCase {

	private static final Logger log = LoggerFactory.getLogger(GrantTokenService.class);

	private final TokenLedgerCommandExecutor commandExecutor;

	public GrantTokenService(TokenLedgerCommandExecutor commandExecutor) {
		this.commandExecutor = commandExecutor;
	}

	@Override
	@Transactional
	public void grant(GrantTokenCommand command) {
		log.info("GrantTokenService : grant : 토큰 지급 요청 - memberUuid={}, transactionUuid={}",
				command.memberUuid(), command.transactionUuid());
		commandExecutor.executeMutation(
				command.transactionUuid(),
				command.memberUuid(),
				wallet -> wallet.grant(
						command.transactionUuid(),
						TransactionType.REWARD,
						TokenCommandSupport.parseReferenceType(command.referenceType()),
						command.amount(),
						TokenCommandSupport.descriptionOrDefault(command.description(), "Token grant"),
						Instant.now()
				)
		);
		log.info("GrantTokenService : grant : 토큰 지급 완료 - memberUuid={}", command.memberUuid());
	}
}
