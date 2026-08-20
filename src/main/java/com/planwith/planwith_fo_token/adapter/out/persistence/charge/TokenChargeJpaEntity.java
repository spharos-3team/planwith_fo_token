package com.planwith.planwith_fo_token.adapter.out.persistence.charge;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "token_charge",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_token_charge_uuid",
				columnNames = {"charge_uuid"}
		)
)
class TokenChargeJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "charge_id")
	private Long chargeId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "wallet_uuid", length = 36)
	private UUID walletUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "payment_method_uuid", length = 36)
	private UUID paymentMethodUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "charge_uuid", nullable = false, length = 36)
	private UUID chargeUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_type", length = 20)
	private PaymentType paymentType;

	@Column(name = "provider_payment_id", length = 255)
	private String providerPaymentId;

	@Column(name = "token_amount", nullable = false)
	private long tokenAmount;

	@Column(name = "billing_key", length = 255)
	private String billingKey;

	@Column(name = "paid_amount", nullable = false)
	private long paidAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ChargeStatus status;

	@Column(name = "charged_at")
	private Instant chargedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected TokenChargeJpaEntity() {
	}

	static TokenChargeJpaEntity create(
			UUID walletUuid,
			UUID paymentMethodUuid,
			UUID chargeUuid,
			PaymentType paymentType,
			String providerPaymentId,
			long tokenAmount,
			String billingKey,
			long paidAmount,
			ChargeStatus status,
			Instant chargedAt,
			Instant createdAt
	) {
		TokenChargeJpaEntity entity = new TokenChargeJpaEntity();
		entity.walletUuid = walletUuid;
		entity.paymentMethodUuid = paymentMethodUuid;
		entity.chargeUuid = chargeUuid;
		entity.paymentType = paymentType;
		entity.providerPaymentId = providerPaymentId;
		entity.tokenAmount = tokenAmount;
		entity.billingKey = billingKey;
		entity.paidAmount = paidAmount;
		entity.status = status;
		entity.chargedAt = chargedAt;
		entity.createdAt = createdAt;
		return entity;
	}

	Long getChargeId() { return chargeId; }
	UUID getWalletUuid() { return walletUuid; }
	UUID getPaymentMethodUuid() { return paymentMethodUuid; }
	UUID getChargeUuid() { return chargeUuid; }
	PaymentType getPaymentType() { return paymentType; }
	String getProviderPaymentId() { return providerPaymentId; }
	long getTokenAmount() { return tokenAmount; }
	String getBillingKey() { return billingKey; }
	long getPaidAmount() { return paidAmount; }
	ChargeStatus getStatus() { return status; }
	Instant getChargedAt() { return chargedAt; }
	Instant getCreatedAt() { return createdAt; }
}
