package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.ConfirmTokenChargeCommand;
import com.planwith.planwith_fo_token.application.port.in.command.ConfirmTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.out.PaymentGatewayPort;
import com.planwith.planwith_fo_token.application.port.out.TokenChargePort;
import com.planwith.planwith_fo_token.application.port.out.payment.PaymentInquiryResult;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;
import com.planwith.planwith_fo_token.application.service.support.PaymentVerifiedTokenGrantSupport;
import com.planwith.planwith_fo_token.domain.exception.InvalidChargeStateException;
import com.planwith.planwith_fo_token.domain.exception.TokenChargeNotFoundException;
import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;

@Service
public class ConfirmTokenChargeService implements ConfirmTokenChargeUseCase {

	private static final Logger log = LoggerFactory.getLogger(ConfirmTokenChargeService.class);

	private final TokenChargePort tokenChargePort;
	private final PaymentGatewayPort paymentGatewayPort;
	private final PaymentVerifiedTokenGrantSupport paymentVerifiedTokenGrantSupport;

	public ConfirmTokenChargeService(
			TokenChargePort tokenChargePort,
			PaymentGatewayPort paymentGatewayPort,
			PaymentVerifiedTokenGrantSupport paymentVerifiedTokenGrantSupport
	) {
		this.tokenChargePort = tokenChargePort;
		this.paymentGatewayPort = paymentGatewayPort;
		this.paymentVerifiedTokenGrantSupport = paymentVerifiedTokenGrantSupport;
	}

	@Override
	@Transactional
	public TokenChargeRequestResult confirm(ConfirmTokenChargeCommand command) {
		log.info(
				"ConfirmTokenChargeService : confirm : 결제 검증 및 토큰 지급 요청 - memberUuid={}, chargeUuid={}, providerPaymentId={}",
				command.memberUuid(),
				command.chargeUuid(),
				command.providerPaymentId()
		);

		if (command.providerPaymentId() == null || command.providerPaymentId().isBlank()) {
			throw new IllegalArgumentException("providerPaymentId is required.");
		}

		TokenCharge charge = tokenChargePort.findByChargeUuidAndMemberUuid(
						command.chargeUuid().value(),
						command.memberUuid()
				)
				.orElseThrow(() -> new TokenChargeNotFoundException(
						"Token charge not found. chargeUuid=" + command.chargeUuid().value()
				));

		if (charge.status() == ChargeStatus.PAID) {
			log.info(
					"ConfirmTokenChargeService : confirm : 이미 결제 완료된 충전 요청 멱등 반환 - chargeUuid={}",
					charge.chargeUuid()
			);
			return PaymentVerifiedTokenGrantSupport.toResult(charge);
		}
		if (charge.status() != ChargeStatus.READY) {
			throw new InvalidChargeStateException(
					"Charge must be READY to confirm payment. status=" + charge.status()
							+ ", chargeUuid=" + charge.chargeUuid().value()
			);
		}

		paymentVerifiedTokenGrantSupport.verifyLocalAmount(charge, command.paidAmount());

		PaymentInquiryResult inquiry = paymentGatewayPort.getPayment(command.providerPaymentId().trim());
		TokenChargeRequestResult result = paymentVerifiedTokenGrantSupport.verifyAndGrant(charge, inquiry);

		log.info(
				"ConfirmTokenChargeService : confirm : 결제 검증 및 토큰 지급 처리 완료 - memberUuid={}, chargeUuid={}, status={}",
				command.memberUuid(),
				result.chargeUuid(),
				result.status()
		);
		return result;
	}
}
