package com.planwith.planwith_fo_token.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.planwith.planwith_fo_token.domain.exception.InvalidChargeStateException;
import com.planwith.planwith_fo_token.domain.model.vo.ChargeUuid;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

public final class TokenCharge {

	private final Long chargeId;
	private final ChargeUuid chargeUuid;
	private final MemberUuid memberUuid;
	private final TokenProductCode productCode;
	private final String clientRequestId;
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
			MemberUuid memberUuid,
			TokenProductCode productCode,
			String clientRequestId,
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
		this.memberUuid = memberUuid;
		this.productCode = productCode;
		this.clientRequestId = clientRequestId;
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

	public static TokenCharge request(
			ChargeUuid chargeUuid,
			MemberUuid memberUuid,
			TokenProductCode productCode,
			String clientRequestId,
			PaymentMethodUuid paymentMethodUuid,
			PaymentType paymentType,
			String billingKey,
			long tokenAmount,
			long paidAmount,
			Instant createdAt
	) {
		if (tokenAmount <= 0) {
			throw new IllegalArgumentException("Token amount must be positive.");
		}
		if (paidAmount <= 0) {
			throw new IllegalArgumentException("Paid amount must be positive.");
		}
		return new TokenCharge(
				null,
				chargeUuid,
				Objects.requireNonNull(memberUuid, "Member UUID is required."),
				Objects.requireNonNull(productCode, "Product code is required."),
				clientRequestId,
				null,
				Objects.requireNonNull(paymentMethodUuid, "Payment method UUID is required."),
				Objects.requireNonNull(paymentType, "Payment type is required."),
				null,
				tokenAmount,
				billingKey,
				paidAmount,
				ChargeStatus.READY,
				null,
				createdAt
		);
	}

	public static TokenCharge restore(
			Long chargeId,
			ChargeUuid chargeUuid,
			MemberUuid memberUuid,
			TokenProductCode productCode,
			String clientRequestId,
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
				memberUuid,
				productCode,
				clientRequestId,
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

	public TokenCharge markPaid(TransactionUuid walletUuid, String providerPaymentId, Instant chargedAt) {
		ensureReady("mark as paid");
		return new TokenCharge(
				chargeId,
				chargeUuid,
				memberUuid,
				productCode,
				clientRequestId,
				Objects.requireNonNull(walletUuid, "Wallet UUID is required."),
				paymentMethodUuid,
				paymentType,
				providerPaymentId,
				tokenAmount,
				billingKey,
				paidAmount,
				ChargeStatus.PAID,
				Objects.requireNonNull(chargedAt, "Charged at is required."),
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

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public TokenProductCode productCode() {
		return productCode;
	}

	public String clientRequestId() {
		return clientRequestId;
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

	private void ensureReady(String action) {
		if (status != ChargeStatus.READY) {
			throw new InvalidChargeStateException(
					"Cannot " + action + " charge in status " + status + ". chargeUuid=" + chargeUuid.value()
			);
		}
	}
}
