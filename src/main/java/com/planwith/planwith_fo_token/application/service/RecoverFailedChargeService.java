package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.RecoverFailedChargeCommand;
import com.planwith.planwith_fo_token.application.port.in.command.RecoverFailedChargeUseCase;

@Service
public class RecoverFailedChargeService implements RecoverFailedChargeUseCase {

	private static final Logger log = LoggerFactory.getLogger(RecoverFailedChargeService.class);

	@Override
	@Transactional
	public void recover(RecoverFailedChargeCommand command) {
		log.info("RecoverFailedChargeService : recover : 토큰 충전 실패 복구 Command 수신 (정책 미구현) - memberUuid={}",
				command.memberUuid());
		throw new UnsupportedOperationException("Token charge recovery policy is not implemented yet.");
	}
}
