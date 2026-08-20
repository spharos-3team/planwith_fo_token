package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.RewardTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.RewardTokenUseCase;

@Service
public class RewardTokenService implements RewardTokenUseCase {

	private static final Logger log = LoggerFactory.getLogger(RewardTokenService.class);

	@Override
	@Transactional
	public void reward(RewardTokenCommand command) {
		log.info("RewardTokenService : reward : 토큰 보상 Command 수신 (정책 미구현) - memberUuid={}",
				command.memberUuid());
		throw new UnsupportedOperationException("Token reward policy is not implemented yet.");
	}
}
