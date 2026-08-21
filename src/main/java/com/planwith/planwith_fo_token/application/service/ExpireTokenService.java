package com.planwith.planwith_fo_token.application.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.ExpireTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.ExpireTokenUseCase;
import com.planwith.planwith_fo_token.application.port.out.LoadTokenWalletPort;
import com.planwith.planwith_fo_token.application.service.support.TokenLedgerCommandExecutor;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;

@Service
public class ExpireTokenService implements ExpireTokenUseCase {

	private static final Logger log = LoggerFactory.getLogger(ExpireTokenService.class);

	private final LoadTokenWalletPort loadTokenWalletPort;
	private final TokenLedgerCommandExecutor commandExecutor;

	public ExpireTokenService(
			LoadTokenWalletPort loadTokenWalletPort,
			TokenLedgerCommandExecutor commandExecutor
	) {
		this.loadTokenWalletPort = loadTokenWalletPort;
		this.commandExecutor = commandExecutor;
	}

	@Override
	@Transactional
	public void expire(ExpireTokenCommand command) {
		log.info(
				"ExpireTokenService : expire : 무료 토큰 만료 요청 - memberUuid={}, transactionUuid={}",
				command.memberUuid(),
				command.transactionUuid()
		);

		TokenWallet wallet = loadTokenWalletPort.load(command.memberUuid());
		if (wallet.getFreeBalance() <= 0) {
			log.info(
					"ExpireTokenService : expire : 만료할 FREE 잔액 없음 - memberUuid={}",
					command.memberUuid()
			);
			return;
		}

		commandExecutor.executeMutation(
				command.transactionUuid(),
				command.memberUuid(),
				lockedWallet -> lockedWallet.expire(command.transactionUuid(), Instant.now())
		);
		log.info("ExpireTokenService : expire : 무료 토큰 만료 완료 - memberUuid={}", command.memberUuid());
	}
}
