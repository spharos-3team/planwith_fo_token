package com.planwith.planwith_fo_token.adapter.out.persistence.payment;

import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

final class PaymentMethodPersistenceMapper {

	private PaymentMethodPersistenceMapper() {
	}

	static PaymentMethod toDomain(PaymentMethodJpaEntity entity) {
		return PaymentMethod.restore(
				entity.getPaymentMethodId(),
				new PaymentMethodUuid(entity.getPaymentMethodUuid()),
				new MemberUuid(entity.getMemberUuid()),
				entity.getBillingKey(),
				entity.getCardName(),
				entity.getFourCardNumber(),
				entity.isDefaultMethod(),
				entity.getStatus(),
				entity.getRegisteredAt()
		);
	}

	static PaymentMethodJpaEntity toEntity(PaymentMethod paymentMethod) {
		return PaymentMethodJpaEntity.create(
				paymentMethod.paymentMethodUuid().value(),
				paymentMethod.memberUuid().value(),
				paymentMethod.billingKey(),
				paymentMethod.cardName(),
				paymentMethod.fourCardNumber(),
				paymentMethod.defaultMethod(),
				paymentMethod.status(),
				paymentMethod.registeredAt()
		);
	}
}
