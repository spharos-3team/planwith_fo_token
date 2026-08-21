package com.planwith.planwith_fo_token.adapter.out.persistence.charge;

import com.planwith.planwith_fo_token.domain.model.TokenCharge;
import com.planwith.planwith_fo_token.domain.model.vo.ChargeUuid;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

final class TokenChargePersistenceMapper {

	private TokenChargePersistenceMapper() {
	}

	static TokenCharge toDomain(TokenChargeJpaEntity entity) {
		return TokenCharge.restore(
				entity.getChargeId(),
				new ChargeUuid(entity.getChargeUuid()),
				entity.getMemberUuid() == null ? null : new MemberUuid(entity.getMemberUuid()),
				entity.getProductCode(),
				entity.getClientRequestId(),
				entity.getWalletUuid() == null ? null : new TransactionUuid(entity.getWalletUuid()),
				entity.getPaymentMethodUuid() == null ? null : new PaymentMethodUuid(entity.getPaymentMethodUuid()),
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
		TokenChargeJpaEntity entity = TokenChargeJpaEntity.create(
				charge.memberUuid() == null ? null : charge.memberUuid().value(),
				charge.productCode(),
				charge.clientRequestId(),
				charge.walletUuid() == null ? null : charge.walletUuid().value(),
				charge.paymentMethodUuid() == null ? null : charge.paymentMethodUuid().value(),
				charge.chargeUuid().value(),
				charge.paymentType(),
				charge.providerPaymentId(),
				charge.tokenAmount(),
				charge.billingKey(),
				charge.paidAmount(),
				charge.status(),
				charge.chargedAt(),
				charge.createdAt()
		);
		if (charge.chargeId() != null) {
			entity.assignId(charge.chargeId());
		}
		return entity;
	}
}
