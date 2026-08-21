package com.planwith.planwith_fo_token.application.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.in.query.ListPaymentMethodsQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.application.query.ListPaymentMethodsQuery;
import com.planwith.planwith_fo_token.application.query.PaymentMethodResult;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;

@Service
public class PaymentMethodQueryService implements ListPaymentMethodsQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(PaymentMethodQueryService.class);

	private final PaymentMethodPort paymentMethodPort;

	public PaymentMethodQueryService(PaymentMethodPort paymentMethodPort) {
		this.paymentMethodPort = paymentMethodPort;
	}

	@Override
	@Transactional(readOnly = true)
	public List<PaymentMethodResult> list(ListPaymentMethodsQuery query) {
		log.info("PaymentMethodQueryService : list : 카드 목록 조회 요청 - memberUuid={}", query.memberUuid());
		List<PaymentMethodResult> results = paymentMethodPort.findActiveByMemberUuid(query.memberUuid()).stream()
				.map(PaymentMethodQueryService::toResult)
				.toList();
		log.info(
				"PaymentMethodQueryService : list : 카드 목록 조회 완료 - memberUuid={}, count={}",
				query.memberUuid(),
				results.size()
		);
		return results;
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
