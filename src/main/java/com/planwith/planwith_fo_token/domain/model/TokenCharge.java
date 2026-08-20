package com.planwith.planwith_fo_token.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.planwith.planwith_fo_token.domain.model.vo.ChargeUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

public final class TokenCharge {

	private final Long chargeId;
	private final ChargeUuid chargeUuid;
	private final TransactionUuid walletUuid;
	private final PaymentMethodUuid paymentMethodUuid;
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
			ChargeUuid chargeUuid,
			TransactionUuid walletUuid,
			PaymentMethodUuid paymentMethodUuid,
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
			ChargeUuid chargeUuid,
			TransactionUuid walletUuid,
			PaymentMethodUuid paymentMethodUuid,
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

	public ChargeUuid chargeUuid() {
		return chargeUuid;
	}

	public TransactionUuid walletUuid() {
		return walletUuid;
	}

	public PaymentMethodUuid paymentMethodUuid() {
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
