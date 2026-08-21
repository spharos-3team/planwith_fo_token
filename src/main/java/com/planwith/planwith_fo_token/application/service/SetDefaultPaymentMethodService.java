package com.planwith.planwith_fo_token.application.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.SetDefaultPaymentMethodCommand;
import com.planwith.planwith_fo_token.application.port.in.command.SetDefaultPaymentMethodUseCase;
import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.application.query.PaymentMethodResult;
import com.planwith.planwith_fo_token.application.service.support.PaymentMethodResultMapper;
import com.planwith.planwith_fo_token.domain.exception.PaymentMethodNotFoundException;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.PaymentMethodStatus;
import com.planwith.planwith_fo_token.domain.service.PaymentMethodPolicy;

@Service
public class SetDefaultPaymentMethodService implements SetDefaultPaymentMethodUseCase {

	private static final Logger log = LoggerFactory.getLogger(SetDefaultPaymentMethodService.class);

	private final PaymentMethodPort paymentMethodPort;

	public SetDefaultPaymentMethodService(PaymentMethodPort paymentMethodPort) {
		this.paymentMethodPort = paymentMethodPort;
	}

	@Override
	@Transactional
	public PaymentMethodResult setDefault(SetDefaultPaymentMethodCommand command) {
		log.info(
				"SetDefaultPaymentMethodService : setDefault : 기본 결제수단 변경 요청 - memberUuid={}, paymentMethodUuid={}",
				command.memberUuid(),
				command.paymentMethodUuid()
		);

		paymentMethodPort.findByUuidAndMemberUuid(command.paymentMethodUuid(), command.memberUuid())
				.filter(method -> method.status() == PaymentMethodStatus.ACTIVE)
				.orElseThrow(() -> new PaymentMethodNotFoundException(
						"Active payment method not found. paymentMethodUuid=" + command.paymentMethodUuid().value()
				));

		List<PaymentMethod> activeMethods = paymentMethodPort.findActiveByMemberUuid(command.memberUuid());
		PaymentMethodPolicy.DefaultChangeResult changeResult = PaymentMethodPolicy.applyDefaultChange(
				activeMethods,
				command.paymentMethodUuid()
		);

		PaymentMethod updatedTarget = null;
		for (PaymentMethod method : changeResult.updatedMethods()) {
			PaymentMethod saved = paymentMethodPort.save(method);
			if (saved.paymentMethodUuid().equals(command.paymentMethodUuid())) {
				updatedTarget = saved;
			}
		}

		log.info(
				"SetDefaultPaymentMethodService : setDefault : 기본 결제수단 변경 완료 - memberUuid={}, paymentMethodUuid={}",
				command.memberUuid(),
				command.paymentMethodUuid()
		);
		return PaymentMethodResultMapper.toResult(updatedTarget);
	}
}
