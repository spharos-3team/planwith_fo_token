package com.planwith.planwith_fo_token.application.service;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.RegisterPaymentMethodCommand;
import com.planwith.planwith_fo_token.application.port.in.command.RegisterPaymentMethodUseCase;
import com.planwith.planwith_fo_token.application.port.out.PaymentGatewayPort;
import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.application.port.out.payment.CardCredential;
import com.planwith.planwith_fo_token.application.port.out.payment.IssueBillingKeyRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.IssueBillingKeyResult;
import com.planwith.planwith_fo_token.application.query.PaymentMethodResult;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;
import com.planwith.planwith_fo_token.domain.service.PaymentMethodPolicy;

@Service
public class RegisterPaymentMethodService implements RegisterPaymentMethodUseCase {

	private static final Logger log = LoggerFactory.getLogger(RegisterPaymentMethodService.class);

	private final PaymentGatewayPort paymentGatewayPort;
	private final PaymentMethodPort paymentMethodPort;

	public RegisterPaymentMethodService(
			PaymentGatewayPort paymentGatewayPort,
			PaymentMethodPort paymentMethodPort
	) {
		this.paymentGatewayPort = paymentGatewayPort;
		this.paymentMethodPort = paymentMethodPort;
	}

	@Override
	@Transactional
	public PaymentMethodResult register(RegisterPaymentMethodCommand command) {
		log.info(
				"RegisterPaymentMethodService : register : 카드 등록 요청 - memberUuid={}, defaultMethod={}",
				command.memberUuid(),
				command.defaultMethod()
		);

		List<PaymentMethod> activeMethods = paymentMethodPort.findActiveByMemberUuid(command.memberUuid());
		boolean asDefault = PaymentMethodPolicy.shouldRegisterAsDefault(activeMethods, command.defaultMethod());

		IssueBillingKeyResult billingKeyResult = paymentGatewayPort.issueBillingKey(new IssueBillingKeyRequest(
				command.memberUuid().toString(),
				null,
				new CardCredential(
						command.cardNumber(),
						command.expiryYear(),
						command.expiryMonth(),
						command.birthOrBusinessRegistrationNumber(),
						command.passwordTwoDigits()
				)
		));

		if (asDefault) {
			for (PaymentMethod method : activeMethods) {
				if (method.defaultMethod()) {
					paymentMethodPort.save(method.clearDefault());
				}
			}
		}

		PaymentMethod registered = paymentMethodPort.save(PaymentMethod.register(
				PaymentMethodUuid.newId(),
				command.memberUuid(),
				billingKeyResult.billingKey(),
				command.cardName(),
				billingKeyResult.fourCardNumber(),
				asDefault,
				Instant.now()
		));

		log.info(
				"RegisterPaymentMethodService : register : 카드 등록 완료 - memberUuid={}, paymentMethodUuid={}, fourCardNumber={}, defaultMethod={}",
				command.memberUuid(),
				registered.paymentMethodUuid(),
				registered.fourCardNumber(),
				registered.defaultMethod()
		);

		return toResult(registered);
	}

	private static PaymentMethodResult toResult(PaymentMethod method) {
		return new PaymentMethodResult(
				method.paymentMethodUuid().value(),
				method.cardName(),
				method.fourCardNumber(),
				method.defaultMethod(),
				method.registeredAt()
		);
	}
}
