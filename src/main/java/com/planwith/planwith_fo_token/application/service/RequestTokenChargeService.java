package com.planwith.planwith_fo_token.application.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.RequestTokenChargeCommand;
import com.planwith.planwith_fo_token.application.port.in.command.RequestTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.application.port.out.TokenChargePort;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;
import com.planwith.planwith_fo_token.domain.exception.PaymentMethodNotFoundException;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.PaymentMethodStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentType;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;
import com.planwith.planwith_fo_token.domain.model.TokenProduct;
import com.planwith.planwith_fo_token.domain.model.vo.ChargeUuid;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;
import com.planwith.planwith_fo_token.domain.service.TokenProductPolicy;

@Service
public class RequestTokenChargeService implements RequestTokenChargeUseCase {

	private static final Logger log = LoggerFactory.getLogger(RequestTokenChargeService.class);

	private final TokenChargePort tokenChargePort;
	private final PaymentMethodPort paymentMethodPort;

	public RequestTokenChargeService(TokenChargePort tokenChargePort, PaymentMethodPort paymentMethodPort) {
		this.tokenChargePort = tokenChargePort;
		this.paymentMethodPort = paymentMethodPort;
	}

	@Override
	@Transactional
	public TokenChargeRequestResult request(RequestTokenChargeCommand command) {
		log.info(
				"RequestTokenChargeService : request : 토큰 충전 요청 생성 - memberUuid={}, productCode={}, paymentType={}, paymentMethodUuid={}",
				command.memberUuid(),
				command.productCode(),
				command.paymentType(),
				command.paymentMethodUuid()
		);

		if (command.clientRequestId() != null && !command.clientRequestId().isBlank()) {
			TokenCharge existing = tokenChargePort.findByMemberUuidAndClientRequestId(
					command.memberUuid(),
					command.clientRequestId()
			).orElse(null);
			if (existing != null) {
				log.info(
						"RequestTokenChargeService : request : 중복 충전 요청 멱등 반환 - memberUuid={}, chargeUuid={}",
						command.memberUuid(),
						existing.chargeUuid()
				);
				return toResult(existing);
			}
		}

		TokenProduct product = TokenProductPolicy.require(command.productCode());
		PaymentType paymentType = command.paymentType() == null
				? PaymentType.BILLING_KEY
				: command.paymentType();

		PaymentMethodUuid paymentMethodUuid = null;
		String billingKey = null;
		if (paymentType == PaymentType.BILLING_KEY) {
			if (command.paymentMethodUuid() == null) {
				throw new IllegalArgumentException("paymentMethodUuid is required for BILLING_KEY payment.");
			}
			PaymentMethod paymentMethod = requireActivePaymentMethod(
					command.paymentMethodUuid(),
					command.memberUuid()
			);
			paymentMethodUuid = paymentMethod.paymentMethodUuid();
			billingKey = paymentMethod.billingKey();
		}

		TokenCharge charge = TokenCharge.request(
				ChargeUuid.newId(),
				command.memberUuid(),
				product.code(),
				blankToNull(command.clientRequestId()),
				paymentMethodUuid,
				paymentType,
				billingKey,
				product.totalTokenAmount(),
				product.salePrice(),
				Instant.now()
		);

		try {
			TokenCharge saved = tokenChargePort.save(charge);
			log.info(
					"RequestTokenChargeService : request : 토큰 충전 READY 생성 완료 - memberUuid={}, chargeUuid={}, paidAmount={}, tokenAmount={}",
					command.memberUuid(),
					saved.chargeUuid(),
					saved.paidAmount(),
					saved.tokenAmount()
			);
			return toResult(saved);
		} catch (DataIntegrityViolationException exception) {
			if (command.clientRequestId() != null && !command.clientRequestId().isBlank()) {
				return tokenChargePort.findByMemberUuidAndClientRequestId(
								command.memberUuid(),
								command.clientRequestId()
						)
						.map(RequestTokenChargeService::toResult)
						.orElseThrow(() -> exception);
			}
			throw exception;
		}
	}

	private PaymentMethod requireActivePaymentMethod(PaymentMethodUuid paymentMethodUuid, MemberUuid memberUuid) {
		return paymentMethodPort.findByUuidAndMemberUuid(paymentMethodUuid, memberUuid)
				.filter(method -> method.status() == PaymentMethodStatus.ACTIVE)
				.orElseThrow(() -> new PaymentMethodNotFoundException(
						"Active payment method not found. paymentMethodUuid=" + paymentMethodUuid.value()
				));
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

	private static String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
