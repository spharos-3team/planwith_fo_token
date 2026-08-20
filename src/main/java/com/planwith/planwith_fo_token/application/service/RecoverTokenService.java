package com.planwith.planwith_fo_token.application.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.RecoverTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.RecoverTokenUseCase;
import com.planwith.planwith_fo_token.application.service.support.TokenCommandSupport;
import com.planwith.planwith_fo_token.application.service.support.TokenLedgerCommandExecutor;
import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TransactionType;

@Service
public class RecoverTokenService implements RecoverTokenUseCase {

	private static final Logger log = LoggerFactory.getLogger(RecoverTokenService.class);

	private final TokenLedgerCommandExecutor commandExecutor;

	public RecoverTokenService(TokenLedgerCommandExecutor commandExecutor) {
		this.commandExecutor = commandExecutor;
	}

	@Override
	@Transactional
	public void recover(RecoverTokenCommand command) {
		log.info("RecoverTokenService : recover : 충전 실패 복구 요청 - memberUuid={}, transactionUuid={}",
				command.memberUuid(), command.transactionUuid());
		commandExecutor.executeMutation(
				command.transactionUuid(),
				command.memberUuid(),
				wallet -> wallet.grant(
						command.transactionUuid(),
						TransactionType.CHARGE,
						ReferenceType.PAYMENT,
						command.amount(),
						TokenCommandSupport.descriptionOrDefault(command.description(), "Token charge recovery"),
						Instant.now()
				)
		);
		log.info("RecoverTokenService : recover : 충전 실패 복구 완료 - memberUuid={}", command.memberUuid());
	}
}
