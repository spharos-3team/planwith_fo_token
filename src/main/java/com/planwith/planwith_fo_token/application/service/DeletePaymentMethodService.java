package com.planwith.planwith_fo_token.application.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.DeletePaymentMethodCommand;
import com.planwith.planwith_fo_token.application.port.in.command.DeletePaymentMethodUseCase;
import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.domain.exception.PaymentMethodNotFoundException;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.PaymentMethodStatus;
import com.planwith.planwith_fo_token.domain.service.PaymentMethodPolicy;

@Service
public class DeletePaymentMethodService implements DeletePaymentMethodUseCase {

	private static final Logger log = LoggerFactory.getLogger(DeletePaymentMethodService.class);

	private final PaymentMethodPort paymentMethodPort;

	public DeletePaymentMethodService(PaymentMethodPort paymentMethodPort) {
		this.paymentMethodPort = paymentMethodPort;
	}

	@Override
	@Transactional
	public void delete(DeletePaymentMethodCommand command) {
		log.info(
				"DeletePaymentMethodService : delete : 카드 삭제 요청 - memberUuid={}, paymentMethodUuid={}",
				command.memberUuid(),
				command.paymentMethodUuid()
		);

		PaymentMethod target = paymentMethodPort.findByUuidAndMemberUuid(
						command.paymentMethodUuid(),
						command.memberUuid()
				)
				.filter(method -> method.status() == PaymentMethodStatus.ACTIVE)
				.orElseThrow(() -> new PaymentMethodNotFoundException(
						"Active payment method not found. paymentMethodUuid=" + command.paymentMethodUuid().value()
				));

		List<PaymentMethod> activeMethods = paymentMethodPort.findActiveByMemberUuid(command.memberUuid());
		PaymentMethodPolicy.DeleteResult deleteResult = PaymentMethodPolicy.applyOnDelete(target, activeMethods);

		paymentMethodPort.save(deleteResult.deleted());
		if (deleteResult.promotedDefault() != null) {
			paymentMethodPort.save(deleteResult.promotedDefault());
			log.info(
					"DeletePaymentMethodService : delete : 기본 결제수단 재지정 - memberUuid={}, paymentMethodUuid={}",
					command.memberUuid(),
					deleteResult.promotedDefault().paymentMethodUuid()
			);
		}

		log.info(
				"DeletePaymentMethodService : delete : 카드 삭제 완료 - memberUuid={}, paymentMethodUuid={}, status=DELETED",
				command.memberUuid(),
				command.paymentMethodUuid()
		);
	}
}
