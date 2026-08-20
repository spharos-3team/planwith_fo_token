package com.planwith.planwith_fo_token.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class TokenCharge {

	private final Long chargeId;
	private final UUID chargeUuid;
	private final UUID walletUuid;
	private final UUID paymentMethodUuid;
	private final PaymentType paymentType;
	private final String providerPaymentId;
	private final long tokenAmount;
	private final String billingKey;
	private final long paidAmount;
	private final ChargeStatus status;
	private final Instant chargedAt;
	private final Instant createdAt;

	private TokenCharge(
			Long chargeId,
			UUID chargeUuid,
			UUID walletUuid,
			UUID paymentMethodUuid,
			PaymentType paymentType,
			String providerPaymentId,
			long tokenAmount,
			String billingKey,
			long paidAmount,
			ChargeStatus status,
			Instant chargedAt,
			Instant createdAt
	) {
		this.chargeId = chargeId;
		this.chargeUuid = Objects.requireNonNull(chargeUuid, "Charge UUID is required.");
		this.walletUuid = walletUuid;
		this.paymentMethodUuid = paymentMethodUuid;
		this.paymentType = paymentType;
		this.providerPaymentId = providerPaymentId;
		this.tokenAmount = tokenAmount;
		this.billingKey = billingKey;
		this.paidAmount = paidAmount;
		this.status = Objects.requireNonNull(status, "Charge status is required.");
		this.chargedAt = chargedAt;
		this.createdAt = Objects.requireNonNull(createdAt, "Created at is required.");
	}

	public static TokenCharge restore(
			Long chargeId,
			UUID chargeUuid,
			UUID walletUuid,
			UUID paymentMethodUuid,
			PaymentType paymentType,
			String providerPaymentId,
			long tokenAmount,
			String billingKey,
			long paidAmount,
			ChargeStatus status,
			Instant chargedAt,
			Instant createdAt
	) {
		return new TokenCharge(
				chargeId,
				chargeUuid,
				walletUuid,
				paymentMethodUuid,
				paymentType,
				providerPaymentId,
				tokenAmount,
				billingKey,
				paidAmount,
				status,
				chargedAt,
				createdAt
		);
	}

	public boolean grantsPaidTokens() {
		return status == ChargeStatus.PAID;
	}

	public Long chargeId() {
		return chargeId;
	}

	public UUID chargeUuid() {
		return chargeUuid;
	}

	public UUID walletUuid() {
		return walletUuid;
	}

	public UUID paymentMethodUuid() {
		return paymentMethodUuid;
	}

	public PaymentType paymentType() {
		return paymentType;
	}

	public String providerPaymentId() {
		return providerPaymentId;
	}

	public long tokenAmount() {
		return tokenAmount;
	}

	public String billingKey() {
		return billingKey;
	}

	public long paidAmount() {
		return paidAmount;
	}

	public ChargeStatus status() {
		return status;
	}

	public Instant chargedAt() {
		return chargedAt;
	}

	public Instant createdAt() {
		return createdAt;
	}
}
