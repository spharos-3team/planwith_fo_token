package com.planwith.planwith_fo_token.adapter.out.persistence.payment;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;

@Component
public class PaymentMethodPersistenceAdapter implements PaymentMethodPort {

	private static final Logger log = LoggerFactory.getLogger(PaymentMethodPersistenceAdapter.class);

	private final SpringDataPaymentMethodRepository repository;

	public PaymentMethodPersistenceAdapter(SpringDataPaymentMethodRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public PaymentMethod save(PaymentMethod paymentMethod) {
		PaymentMethodJpaEntity saved = repository.save(PaymentMethodPersistenceMapper.toEntity(paymentMethod));
		log.debug("PaymentMethodPersistenceAdapter : save : 결제수단 저장 - paymentMethodUuid={}",
				paymentMethod.paymentMethodUuid());
		return PaymentMethodPersistenceMapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<PaymentMethod> findByUuid(UUID paymentMethodUuid) {
		return repository.findByPaymentMethodUuid(paymentMethodUuid)
				.map(PaymentMethodPersistenceMapper::toDomain);
	}
}
