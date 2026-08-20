package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.ChargeTokenCommand;
import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.ChargeTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.GrantTokenUseCase;

@Service
public class ChargeTokenService implements ChargeTokenUseCase {

	private static final Logger log = LoggerFactory.getLogger(ChargeTokenService.class);

	private final GrantTokenUseCase grantTokenUseCase;

	public ChargeTokenService(GrantTokenUseCase grantTokenUseCase) {
		this.grantTokenUseCase = grantTokenUseCase;
	}

	@Override
	@Transactional
	public void charge(ChargeTokenCommand command) {
		log.info("ChargeTokenService : charge : 유료 결제 토큰 지급 위임 - memberUuid={}, transactionUuid={}",
				command.memberUuid(), command.transactionUuid());
		grantTokenUseCase.grant(GrantTokenCommand.fromCharge(command));
		log.info("ChargeTokenService : charge : 유료 결제 토큰 지급 완료 - memberUuid={}", command.memberUuid());
	}
}
