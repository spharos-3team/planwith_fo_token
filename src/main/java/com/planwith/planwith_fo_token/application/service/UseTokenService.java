package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.UseTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.UseTokenUseCase;

@Service
public class UseTokenService implements UseTokenUseCase {

	private static final Logger log = LoggerFactory.getLogger(UseTokenService.class);

	@Override
	@Transactional
	public void use(UseTokenCommand command) {
		log.info("UseTokenService : use : 토큰 사용 Command 수신 (정책 미구현) - memberUuid={}",
				command.memberUuid());
		throw new UnsupportedOperationException("Token use policy is not implemented yet.");
	}
}
