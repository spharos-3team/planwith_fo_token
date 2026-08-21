package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.PayTokenChargeCommand;
import com.planwith.planwith_fo_token.application.port.in.command.PayTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.out.PaymentGatewayPort;
import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.application.port.out.TokenChargePort;
import com.planwith.planwith_fo_token.application.port.out.payment.PayRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.PayResult;
import com.planwith.planwith_fo_token.application.port.out.payment.PayWithBillingKeyRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.PaymentInquiryResult;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;
import com.planwith.planwith_fo_token.application.service.support.PaymentVerifiedTokenGrantSupport;
import com.planwith.planwith_fo_token.domain.exception.InvalidChargeStateException;
import com.planwith.planwith_fo_token.domain.exception.PaymentMethodNotFoundException;
import com.planwith.planwith_fo_token.domain.exception.TokenChargeNotFoundException;
import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.PaymentMethodStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentType;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;
import com.planwith.planwith_fo_token.domain.service.TokenProductPolicy;

@Service
public class PayTokenChargeService implements PayTokenChargeUseCase {

	private static final Logger log = LoggerFactory.getLogger(PayTokenChargeService.class);
	private static final String CURRENCY = "KRW";

	private final TokenChargePort tokenChargePort;
	private final PaymentMethodPort paymentMethodPort;
	private final PaymentGatewayPort paymentGatewayPort;
	private final PaymentVerifiedTokenGrantSupport paymentVerifiedTokenGrantSupport;

	public PayTokenChargeService(
			TokenChargePort tokenChargePort,
			PaymentMethodPort paymentMethodPort,
			PaymentGatewayPort paymentGatewayPort,
			PaymentVerifiedTokenGrantSupport paymentVerifiedTokenGrantSupport
	) {
		this.tokenChargePort = tokenChargePort;
		this.paymentMethodPort = paymentMethodPort;
		this.paymentGatewayPort = paymentGatewayPort;
		this.paymentVerifiedTokenGrantSupport = paymentVerifiedTokenGrantSupport;
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
			return PaymentVerifiedTokenGrantSupport.toResult(charge);
		}
		if (charge.status() != ChargeStatus.READY) {
			throw new InvalidChargeStateException(
					"Charge must be READY to pay. status=" + charge.status()
							+ ", chargeUuid=" + charge.chargeUuid().value()
			);
		}

		paymentVerifiedTokenGrantSupport.verifyLocalAmount(charge, command.paidAmount());
		PayResult payResult = executePayment(charge);

		log.info(
				"PayTokenChargeService : pay : PG 결제 응답 수신 후 서버 재조회 시작 - chargeUuid={}, providerPaymentId={}",
				charge.chargeUuid(),
				payResult.paymentId()
		);
		PaymentInquiryResult inquiry = paymentGatewayPort.getPayment(payResult.paymentId());
		TokenChargeRequestResult result = paymentVerifiedTokenGrantSupport.verifyAndGrant(charge, inquiry);

		log.info(
				"PayTokenChargeService : pay : 토큰 충전 결제 처리 완료 - memberUuid={}, chargeUuid={}, status={}, tokenAmount={}",
				command.memberUuid(),
				result.chargeUuid(),
				result.status(),
				result.tokenAmount()
		);
		return result;
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

	private static String resolveOrderName(TokenCharge charge) {
		if (charge.productCode() == null) {
			return "토큰 충전";
		}
		return TokenProductPolicy.require(charge.productCode()).name();
	}
}
