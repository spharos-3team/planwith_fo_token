package com.planwith.planwith_fo_token.application.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.ChargeTokenCommand;
import com.planwith.planwith_fo_token.application.command.PayTokenChargeCommand;
import com.planwith.planwith_fo_token.application.exception.PaymentGatewayException;
import com.planwith.planwith_fo_token.application.port.in.command.ChargeTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.PayTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.out.PaymentGatewayPort;
import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.application.port.out.TokenChargePort;
import com.planwith.planwith_fo_token.application.port.out.payment.PayRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.PayResult;
import com.planwith.planwith_fo_token.application.port.out.payment.PayWithBillingKeyRequest;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;
import com.planwith.planwith_fo_token.domain.exception.ChargeAmountMismatchException;
import com.planwith.planwith_fo_token.domain.exception.InvalidChargeStateException;
import com.planwith.planwith_fo_token.domain.exception.PaymentMethodNotFoundException;
import com.planwith.planwith_fo_token.domain.exception.TokenChargeNotFoundException;
import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.PaymentMethodStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentType;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;
import com.planwith.planwith_fo_token.domain.model.TokenProduct;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;
import com.planwith.planwith_fo_token.domain.service.TokenProductPolicy;

@Service
public class PayTokenChargeService implements PayTokenChargeUseCase {

	private static final Logger log = LoggerFactory.getLogger(PayTokenChargeService.class);
	private static final String CURRENCY = "KRW";

	private final TokenChargePort tokenChargePort;
	private final PaymentMethodPort paymentMethodPort;
	private final PaymentGatewayPort paymentGatewayPort;
	private final ChargeTokenUseCase chargeTokenUseCase;

	public PayTokenChargeService(
			TokenChargePort tokenChargePort,
			PaymentMethodPort paymentMethodPort,
			PaymentGatewayPort paymentGatewayPort,
			ChargeTokenUseCase chargeTokenUseCase
	) {
		this.tokenChargePort = tokenChargePort;
		this.paymentMethodPort = paymentMethodPort;
		this.paymentGatewayPort = paymentGatewayPort;
		this.chargeTokenUseCase = chargeTokenUseCase;
	}

	@Override
	@Transactional
	public TokenChargeRequestResult pay(PayTokenChargeCommand command) {
		log.info(
				"PayTokenChargeService : pay : 토큰 충전 결제 요청 - memberUuid={}, chargeUuid={}, paymentAmount={}",
				command.memberUuid(),
				command.chargeUuid(),
				command.paidAmount()
		);

		TokenCharge charge = tokenChargePort.findByChargeUuidAndMemberUuid(
						command.chargeUuid().value(),
						command.memberUuid()
				)
				.orElseThrow(() -> new TokenChargeNotFoundException(
						"Token charge not found. chargeUuid=" + command.chargeUuid().value()
				));

		if (charge.status() == ChargeStatus.PAID) {
			log.info(
					"PayTokenChargeService : pay : 이미 결제 완료된 충전 요청 멱등 반환 - chargeUuid={}",
					charge.chargeUuid()
			);
			return toResult(charge);
		}
		if (charge.status() != ChargeStatus.READY) {
			throw new InvalidChargeStateException(
					"Charge must be READY to pay. status=" + charge.status()
							+ ", chargeUuid=" + charge.chargeUuid().value()
			);
		}

		verifyAmount(charge, command.paidAmount());
		PayResult payResult = executePayment(charge);
		if (!"PAID".equalsIgnoreCase(payResult.status())) {
			throw new PaymentGatewayException(
					"Payment gateway did not complete payment. status=" + payResult.status()
							+ ", chargeUuid=" + charge.chargeUuid().value()
			);
		}

		TransactionUuid ledgerTransactionUuid = new TransactionUuid(charge.chargeUuid().value());
		chargeTokenUseCase.charge(new ChargeTokenCommand(
				ledgerTransactionUuid,
				command.memberUuid(),
				charge.tokenAmount(),
				"PAYMENT",
				charge.chargeUuid().toString(),
				"token charge " + (charge.productCode() == null ? "" : charge.productCode().name())
		));

		Instant paidAt = payResult.paidAt() == null ? Instant.now() : payResult.paidAt();
		TokenCharge paid = tokenChargePort.save(charge.markPaid(
				ledgerTransactionUuid,
				payResult.paymentId(),
				paidAt
		));

		log.info(
				"PayTokenChargeService : pay : 토큰 충전 결제 완료 - memberUuid={}, chargeUuid={}, paymentType={}, tokenAmount={}",
				command.memberUuid(),
				paid.chargeUuid(),
				paid.paymentType(),
				paid.tokenAmount()
		);
		return toResult(paid);
	}

	private PayResult executePayment(TokenCharge charge) {
		String paymentId = charge.chargeUuid().toString();
		String orderName = resolveOrderName(charge);
		if (charge.paymentType() == PaymentType.ONE_TIME) {
			log.info("PayTokenChargeService : executePayment : 일회성 PG 결제 시작 - chargeUuid={}", charge.chargeUuid());
			return paymentGatewayPort.pay(new PayRequest(
					paymentId,
					orderName,
					charge.paidAmount(),
					CURRENCY,
					null
			));
		}
		if (charge.paymentType() == PaymentType.BILLING_KEY) {
			PaymentMethod paymentMethod = requireActivePaymentMethod(charge);
			String billingKey = paymentMethod.billingKey() != null
					? paymentMethod.billingKey()
					: charge.billingKey();
			if (billingKey == null || billingKey.isBlank()) {
				throw new InvalidChargeStateException(
						"Billing key is missing for BILLING_KEY payment. chargeUuid=" + charge.chargeUuid().value()
				);
			}
			log.info(
					"PayTokenChargeService : executePayment : 등록카드 BillingKey 결제 시작 - chargeUuid={}, paymentMethodUuid={}",
					charge.chargeUuid(),
					paymentMethod.paymentMethodUuid()
			);
			return paymentGatewayPort.payWithBillingKey(new PayWithBillingKeyRequest(
					paymentId,
					billingKey,
					orderName,
					charge.paidAmount(),
					CURRENCY,
					null
			));
		}
		throw new InvalidChargeStateException(
				"Unsupported payment type. paymentType=" + charge.paymentType()
						+ ", chargeUuid=" + charge.chargeUuid().value()
		);
	}

	private PaymentMethod requireActivePaymentMethod(TokenCharge charge) {
		if (charge.paymentMethodUuid() == null) {
			throw new PaymentMethodNotFoundException(
					"Payment method is required for BILLING_KEY payment. chargeUuid=" + charge.chargeUuid().value()
			);
		}
		return paymentMethodPort.findByUuidAndMemberUuid(charge.paymentMethodUuid(), charge.memberUuid())
				.filter(method -> method.status() == PaymentMethodStatus.ACTIVE)
				.orElseThrow(() -> new PaymentMethodNotFoundException(
						"Active payment method not found. paymentMethodUuid=" + charge.paymentMethodUuid().value()
				));
	}

	private void verifyAmount(TokenCharge charge, Long requestedPaidAmount) {
		if (charge.productCode() != null) {
			TokenProduct product = TokenProductPolicy.require(charge.productCode());
			if (product.salePrice() != charge.paidAmount()) {
				throw new ChargeAmountMismatchException(
						"Charge paidAmount does not match product policy. chargeUuid="
								+ charge.chargeUuid().value()
				);
			}
		}
		if (requestedPaidAmount != null && requestedPaidAmount != charge.paidAmount()) {
			throw new ChargeAmountMismatchException(
					"Requested paidAmount does not match charge. expected="
							+ charge.paidAmount()
							+ ", actual="
							+ requestedPaidAmount
			);
		}
	}

	private static String resolveOrderName(TokenCharge charge) {
		if (charge.productCode() == null) {
			return "토큰 충전";
		}
		return TokenProductPolicy.require(charge.productCode()).name();
	}

	private static TokenChargeRequestResult toResult(TokenCharge charge) {
		return new TokenChargeRequestResult(
				charge.chargeUuid().value(),
				charge.productCode(),
				charge.status(),
				charge.tokenAmount(),
				charge.paidAmount(),
				charge.paymentMethodUuid() == null ? null : charge.paymentMethodUuid().value(),
				charge.paymentType(),
				charge.createdAt()
		);
	}
}
