package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.ChargeTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.ChargeTokenUseCase;

@Service
public class ChargeTokenService implements ChargeTokenUseCase {

	private static final Logger log = LoggerFactory.getLogger(ChargeTokenService.class);

	@Override
	@Transactional
	public void charge(ChargeTokenCommand command) {
		log.info("ChargeTokenService : charge : 토큰 충전 Command 수신 (정책 미구현) - memberUuid={}",
				command.memberUuid());
		throw new UnsupportedOperationException("Token charge policy is not implemented yet.");
	}
}
