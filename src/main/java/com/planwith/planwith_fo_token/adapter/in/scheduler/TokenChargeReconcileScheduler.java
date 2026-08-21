package com.planwith.planwith_fo_token.adapter.in.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_token.application.port.in.command.ReconcileTokenChargeUseCase;

@Component
@ConditionalOnProperty(name = "token.charge.reconcile.enabled", havingValue = "true")
public class TokenChargeReconcileScheduler {

	private static final Logger log = LoggerFactory.getLogger(TokenChargeReconcileScheduler.class);

	private final ReconcileTokenChargeUseCase reconcileTokenChargeUseCase;

	public TokenChargeReconcileScheduler(ReconcileTokenChargeUseCase reconcileTokenChargeUseCase) {
		this.reconcileTokenChargeUseCase = reconcileTokenChargeUseCase;
	}

	@Scheduled(
			fixedDelayString = "${token.charge.reconcile.interval:1m}",
			initialDelayString = "${token.charge.reconcile.initial-delay:30s}"
	)
	public void reconcileStaleReadyCharges() {
		log.info("TokenChargeReconcileScheduler : reconcileStaleReadyCharges : READY 충전 자동 복구 스케줄 시작");
		int recovered = reconcileTokenChargeUseCase.reconcileStaleReadyCharges();
		log.info(
				"TokenChargeReconcileScheduler : reconcileStaleReadyCharges : READY 충전 자동 복구 스케줄 완료 - recovered={}",
				recovered
		);
	}
}
