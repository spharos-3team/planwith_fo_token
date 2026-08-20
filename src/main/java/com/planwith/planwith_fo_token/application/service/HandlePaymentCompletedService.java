package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;
import com.planwith.planwith_fo_token.application.command.HandlePaymentCompletedCommand;
import com.planwith.planwith_fo_token.application.event.TokenChargedEvent;
import com.planwith.planwith_fo_token.application.port.in.HandlePaymentCompletedUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.GrantTokenUseCase;
import com.planwith.planwith_fo_token.application.port.out.ProcessedTokenEventPort;
import com.planwith.planwith_fo_token.domain.model.ProcessedTokenEvent;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

@Service
public class HandlePaymentCompletedService implements HandlePaymentCompletedUseCase {

	private static final Logger log = LoggerFactory.getLogger(HandlePaymentCompletedService.class);

	private final ProcessedTokenEventPort processedTokenEventPort;
	private final GrantTokenUseCase grantTokenUseCase;

	public HandlePaymentCompletedService(
			ProcessedTokenEventPort processedTokenEventPort,
			GrantTokenUseCase grantTokenUseCase
	) {
		this.processedTokenEventPort = processedTokenEventPort;
		this.grantTokenUseCase = grantTokenUseCase;
	}

	@Override
	@Transactional
	public void handle(HandlePaymentCompletedCommand command) {
		if (processedTokenEventPort.existsByEventUuid(command.eventUuid())) {
			log.warn("HandlePaymentCompletedService : handle : 중복 PaymentCompleted 이벤트 무시 - eventUuid={}",
					command.eventUuid());
			return;
		}
		log.info("HandlePaymentCompletedService : handle : PaymentCompleted 토큰 지급 시작 - eventUuid={}, memberUuid={}",
				command.eventUuid(), command.memberUuid());
		grantTokenUseCase.grant(GrantTokenCommand.paidCharge(
				new TransactionUuid(command.eventUuid()),
				command.memberUuid(),
				command.tokenAmount(),
				command.paymentReference(),
				"Payment completed token grant"
		));
		boolean recorded = processedTokenEventPort.saveIdempotent(ProcessedTokenEvent.recorded(
				command.eventUuid(),
				command.memberUuid(),
				TokenChargedEvent.EVENT_TYPE,
				command.completedAt()
		));
		if (!recorded) {
			log.warn("HandlePaymentCompletedService : handle : PaymentCompleted 처리 기록 경합 - eventUuid={}",
					command.eventUuid());
		}
		log.info("HandlePaymentCompletedService : handle : PaymentCompleted 토큰 지급 완료 - eventUuid={}",
				command.eventUuid());
	}
}
