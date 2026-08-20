package com.planwith.planwith_fo_token.adapter.out.persistence.charge;

import com.planwith.planwith_fo_token.domain.model.TokenCharge;

final class TokenChargePersistenceMapper {

	private TokenChargePersistenceMapper() {
	}

	static TokenCharge toDomain(TokenChargeJpaEntity entity) {
		return TokenCharge.restore(
				entity.getChargeId(),
				entity.getChargeUuid(),
				entity.getWalletUuid(),
				entity.getPaymentMethodUuid(),
				entity.getPaymentType(),
				entity.getProviderPaymentId(),
				entity.getTokenAmount(),
				entity.getBillingKey(),
				entity.getPaidAmount(),
				entity.getStatus(),
				entity.getChargedAt(),
				entity.getCreatedAt()
		);
	}

	static TokenChargeJpaEntity toEntity(TokenCharge charge) {
		return TokenChargeJpaEntity.create(
				charge.walletUuid(),
				charge.paymentMethodUuid(),
				charge.chargeUuid(),
				charge.paymentType(),
				charge.providerPaymentId(),
				charge.tokenAmount(),
				charge.billingKey(),
				charge.paidAmount(),
				charge.status(),
				charge.chargedAt(),
				charge.createdAt()
		);
	}
}
