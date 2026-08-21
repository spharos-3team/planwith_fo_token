package com.planwith.planwith_fo_token.application.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.planwith.planwith_fo_token.application.command.ChargeTokenCommand;
import com.planwith.planwith_fo_token.application.command.ReconcileTokenChargeCommand;
import com.planwith.planwith_fo_token.application.port.in.command.ChargeTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.ReconcileTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.out.LoadTokenLedgerPort;
import com.planwith.planwith_fo_token.application.port.out.PaymentGatewayPort;
import com.planwith.planwith_fo_token.application.port.out.TokenChargePort;
import com.planwith.planwith_fo_token.application.port.out.TokenChargeReconcileStatePort;
import com.planwith.planwith_fo_token.application.port.out.payment.PaymentInquiryResult;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;
import com.planwith.planwith_fo_token.application.service.support.PaymentInquiryStatus;
import com.planwith.planwith_fo_token.application.service.support.PaymentVerifiedTokenGrantSupport;
import com.planwith.planwith_fo_token.config.TokenChargeReconcileProperties;
import com.planwith.planwith_fo_token.domain.exception.InvalidChargeStateException;
import com.planwith.planwith_fo_token.domain.exception.TokenChargeNotFoundException;
import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

@Service
public class ReconcileTokenChargeService implements ReconcileTokenChargeUseCase {

	private static final Logger log = LoggerFactory.getLogger(ReconcileTokenChargeService.class);

	private final TokenChargePort tokenChargePort;
	private final PaymentGatewayPort paymentGatewayPort;
	private final PaymentVerifiedTokenGrantSupport paymentVerifiedTokenGrantSupport;
	private final ChargeTokenUseCase chargeTokenUseCase;
	private final LoadTokenLedgerPort loadTokenLedgerPort;
	private final TokenChargeReconcileStatePort reconcileStatePort;
	private final TokenChargeReconcileProperties reconcileProperties;
	private final TransactionTemplate transactionTemplate;
	private final Clock clock;

	public ReconcileTokenChargeService(
			TokenChargePort tokenChargePort,
			PaymentGatewayPort paymentGatewayPort,
			PaymentVerifiedTokenGrantSupport paymentVerifiedTokenGrantSupport,
			ChargeTokenUseCase chargeTokenUseCase,
			LoadTokenLedgerPort loadTokenLedgerPort,
			TokenChargeReconcileStatePort reconcileStatePort,
			TokenChargeReconcileProperties reconcileProperties,
			PlatformTransactionManager transactionManager,
			ObjectProvider<Clock> clockProvider
	) {
		this.tokenChargePort = tokenChargePort;
		this.paymentGatewayPort = paymentGatewayPort;
		this.paymentVerifiedTokenGrantSupport = paymentVerifiedTokenGrantSupport;
		this.chargeTokenUseCase = chargeTokenUseCase;
		this.loadTokenLedgerPort = loadTokenLedgerPort;
		this.reconcileStatePort = reconcileStatePort;
		this.reconcileProperties = reconcileProperties;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		Clock provided = clockProvider.getIfAvailable();
		this.clock = provided == null ? Clock.systemUTC() : provided;
	}

	@Override
	@Transactional
	public TokenChargeRequestResult reconcile(ReconcileTokenChargeCommand command) {
		log.info(
				"ReconcileTokenChargeService : reconcile : 충전 실패 복구 요청 - memberUuid={}, chargeUuid={}",
				command.memberUuid(),
				command.chargeUuid()
		);

		TokenCharge charge = tokenChargePort.findByChargeUuidAndMemberUuid(
						command.chargeUuid().value(),
						command.memberUuid()
				)
				.orElseThrow(() -> new TokenChargeNotFoundException(
						"Token charge not found. chargeUuid=" + command.chargeUuid().value()
				));

		TokenChargeRequestResult result = reconcileCharge(charge);
		log.info(
				"ReconcileTokenChargeService : reconcile : 충전 실패 복구 완료 - chargeUuid={}, status={}",
				result.chargeUuid(),
				result.status()
		);
		return result;
	}

	@Override
	public int reconcileStaleReadyCharges() {
		Instant now = clock.instant();
		Instant createdBefore = now.minus(reconcileProperties.resolvedStaleAfter());
		List<TokenCharge> staleCharges = tokenChargePort.findStaleReadyCharges(
				createdBefore,
				0,
				reconcileProperties.resolvedBatchSize()
		);
		log.info(
				"ReconcileTokenChargeService : reconcileStaleReadyCharges : 미완료 READY 충전 복구 시작 - count={}, createdBefore={}",
				staleCharges.size(),
				createdBefore
		);

		int recovered = 0;
		for (TokenCharge charge : staleCharges) {
			int retryCount = reconcileStatePort.currentRetryCount(charge.chargeUuid().value());
			if (retryCount >= reconcileProperties.resolvedMaxRetry()) {
				log.warn(
						"ReconcileTokenChargeService : reconcileStaleReadyCharges : 최대 재시도 초과로 스킵 - chargeUuid={}, retryCount={}",
						charge.chargeUuid(),
						retryCount
				);
				continue;
			}
			try {
				TokenChargeRequestResult result = transactionTemplate.execute(status -> reconcileCharge(
						tokenChargePort.findByChargeUuid(charge.chargeUuid().value()).orElse(charge)
				));
				if (result != null && result.status() == ChargeStatus.PAID) {
					recovered++;
				}
			} catch (RuntimeException exception) {
				log.warn(
						"ReconcileTokenChargeService : reconcileStaleReadyCharges : 개별 복구 실패 - chargeUuid={}",
						charge.chargeUuid()
				);
			}
		}
		log.info(
				"ReconcileTokenChargeService : reconcileStaleReadyCharges : 미완료 READY 충전 복구 종료 - recovered={}",
				recovered
		);
		return recovered;
	}

	private TokenChargeRequestResult reconcileCharge(TokenCharge charge) {
		Instant now = clock.instant();
		if (charge.status() == ChargeStatus.PAID) {
			return recoverPaidWithoutLedger(charge, now);
		}
		if (charge.status() != ChargeStatus.READY) {
			throw new InvalidChargeStateException(
					"Charge must be READY or PAID to reconcile. status=" + charge.status()
							+ ", chargeUuid=" + charge.chargeUuid().value()
			);
		}

		String providerPaymentId = resolveProviderPaymentId(charge);
		try {
			PaymentInquiryResult inquiry = paymentGatewayPort.getPayment(providerPaymentId);
			PaymentInquiryStatus status = PaymentInquiryStatus.from(inquiry.status());
			if (status != PaymentInquiryStatus.PAID) {
				reconcileStatePort.markFailed(
						charge.chargeUuid().value(),
						"PG_" + status.name(),
						now,
						now.plus(Duration.ofMinutes(1))
				);
				log.warn(
						"ReconcileTokenChargeService : reconcileCharge : PG 미결제 상태로 복구 보류 - chargeUuid={}, pgStatus={}",
						charge.chargeUuid(),
						inquiry.status()
				);
				return PaymentVerifiedTokenGrantSupport.toResult(charge);
			}

			TokenChargeRequestResult result = paymentVerifiedTokenGrantSupport.verifyAndGrant(charge, inquiry);
			if (result.status() == ChargeStatus.PAID) {
				reconcileStatePort.markSucceeded(charge.chargeUuid().value(), now);
			} else {
				reconcileStatePort.markFailed(
						charge.chargeUuid().value(),
						result.status().name(),
						now,
						now.plus(Duration.ofMinutes(1))
				);
			}
			return result;
		} catch (RuntimeException exception) {
			reconcileStatePort.markFailed(
					charge.chargeUuid().value(),
					"ERROR",
					now,
					now.plus(Duration.ofMinutes(1))
			);
			throw exception;
		}
	}

	private TokenChargeRequestResult recoverPaidWithoutLedger(TokenCharge charge, Instant now) {
		TransactionUuid ledgerTx = new TransactionUuid(charge.chargeUuid().value());
		if (loadTokenLedgerPort.existsByTransactionUuid(ledgerTx)) {
			reconcileStatePort.markSucceeded(charge.chargeUuid().value(), now);
			return PaymentVerifiedTokenGrantSupport.toResult(charge);
		}
		log.warn(
				"ReconcileTokenChargeService : recoverPaidWithoutLedger : PAID이나 Ledger 없음 - 토큰 재지급 - chargeUuid={}",
				charge.chargeUuid()
		);
		chargeTokenUseCase.charge(new ChargeTokenCommand(
				ledgerTx,
				charge.memberUuid(),
				charge.tokenAmount(),
				"PAYMENT",
				charge.chargeUuid().toString(),
				"reconcile paid charge grant"
		));
		reconcileStatePort.markSucceeded(charge.chargeUuid().value(), now);
		return PaymentVerifiedTokenGrantSupport.toResult(charge);
	}

	private static String resolveProviderPaymentId(TokenCharge charge) {
		if (charge.providerPaymentId() != null && !charge.providerPaymentId().isBlank()) {
			return charge.providerPaymentId();
		}
		return charge.chargeUuid().toString();
	}
}
