package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.ExpireTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.ExpireTokenUseCase;

@Service
public class ExpireTokenService implements ExpireTokenUseCase {

	private static final Logger log = LoggerFactory.getLogger(ExpireTokenService.class);

	@Override
	@Transactional
	public void expire(ExpireTokenCommand command) {
		log.info("ExpireTokenService : expire : 토큰 만료 Command 수신 (정책 미구현) - memberUuid={}",
				command.memberUuid());
		throw new UnsupportedOperationException("Token expire policy is not implemented yet.");
	}
}
