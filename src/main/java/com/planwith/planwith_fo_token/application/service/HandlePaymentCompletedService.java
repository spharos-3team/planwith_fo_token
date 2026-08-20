package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.HandlePaymentCompletedCommand;
import com.planwith.planwith_fo_token.application.port.in.HandlePaymentCompletedUseCase;
import com.planwith.planwith_fo_token.application.port.out.ProcessedTokenEventPort;

@Service
public class HandlePaymentCompletedService implements HandlePaymentCompletedUseCase {

	private static final Logger log = LoggerFactory.getLogger(HandlePaymentCompletedService.class);

	private final ProcessedTokenEventPort processedTokenEventPort;

	public HandlePaymentCompletedService(ProcessedTokenEventPort processedTokenEventPort) {
		this.processedTokenEventPort = processedTokenEventPort;
	}

	@Override
	@Transactional
	public void handle(HandlePaymentCompletedCommand command) {
		if (processedTokenEventPort.existsByEventUuid(command.eventUuid())) {
			log.warn("HandlePaymentCompletedService : handle : 중복 PaymentCompleted 이벤트 무시 - eventUuid={}",
					command.eventUuid());
			return;
		}
		log.info("HandlePaymentCompletedService : handle : PaymentCompleted 이벤트 수신 (충전 정책 미구현) - eventUuid={}, memberUuid={}",
				command.eventUuid(), command.memberUuid());
	}
}
