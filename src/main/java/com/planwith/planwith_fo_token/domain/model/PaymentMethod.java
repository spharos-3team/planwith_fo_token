package com.planwith.planwith_fo_token.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.planwith.planwith_fo_token.domain.exception.InvalidPaymentMethodStateException;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

public final class PaymentMethod {

	private final Long paymentMethodId;
	private final PaymentMethodUuid paymentMethodUuid;
	private final MemberUuid memberUuid;
	private final String billingKey;
	private final String cardName;
	private final String fourCardNumber;
	private final boolean defaultMethod;
	private final PaymentMethodStatus status;
	private final Instant registeredAt;

	private PaymentMethod(
			Long paymentMethodId,
			PaymentMethodUuid paymentMethodUuid,
			MemberUuid memberUuid,
			String billingKey,
			String cardName,
			String fourCardNumber,
			boolean defaultMethod,
			PaymentMethodStatus status,
			Instant registeredAt
	) {
		this.paymentMethodId = paymentMethodId;
		this.paymentMethodUuid = Objects.requireNonNull(paymentMethodUuid, "Payment method UUID is required.");
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.billingKey = Objects.requireNonNull(billingKey, "Billing key is required.");
		this.cardName = cardName;
		this.fourCardNumber = fourCardNumber;
		this.defaultMethod = defaultMethod;
		this.status = Objects.requireNonNull(status, "Payment method status is required.");
		this.registeredAt = Objects.requireNonNull(registeredAt, "Registered at is required.");
		validateDefaultStatusCombination(defaultMethod, status);
	}

	public static PaymentMethod register(
			PaymentMethodUuid paymentMethodUuid,
			MemberUuid memberUuid,
			String billingKey,
			String cardName,
			String fourCardNumber,
			boolean defaultMethod,
			Instant registeredAt
	) {
		return new PaymentMethod(
				null,
				paymentMethodUuid,
				memberUuid,
				billingKey,
				cardName,
				fourCardNumber,
				defaultMethod,
				PaymentMethodStatus.ACTIVE,
				registeredAt
		);
	}

	public static PaymentMethod restore(
			Long paymentMethodId,
			PaymentMethodUuid paymentMethodUuid,
			MemberUuid memberUuid,
			String billingKey,
			String cardName,
			String fourCardNumber,
			boolean defaultMethod,
			PaymentMethodStatus status,
			Instant registeredAt
	) {
		return new PaymentMethod(
				paymentMethodId,
				paymentMethodUuid,
				memberUuid,
				billingKey,
				cardName,
				fourCardNumber,
				defaultMethod,
				status,
				registeredAt
		);
	}

	public PaymentMethod markAsDefault() {
		ensureActive("mark as default");
		if (defaultMethod) {
			return this;
		}
		return copy(true, status);
	}

	public PaymentMethod clearDefault() {
		if (!defaultMethod) {
			return this;
		}
		return copy(false, status);
	}

	public PaymentMethod delete() {
		ensureActive("delete");
		return copy(false, PaymentMethodStatus.DELETED);
	}

	public PaymentMethod expire() {
		ensureActive("expire");
		return copy(false, PaymentMethodStatus.EXPIRED);
	}

	public Long paymentMethodId() {
		return paymentMethodId;
	}

	public PaymentMethodUuid paymentMethodUuid() {
		return paymentMethodUuid;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public String billingKey() {
		return billingKey;
	}

	public String cardName() {
		return cardName;
	}

	public String fourCardNumber() {
		return fourCardNumber;
	}

	public boolean defaultMethod() {
		return defaultMethod;
	}

	public PaymentMethodStatus status() {
		return status;
	}

	public Instant registeredAt() {
		return registeredAt;
	}

	private void ensureActive(String action) {
		if (status != PaymentMethodStatus.ACTIVE) {
			throw new InvalidPaymentMethodStateException(
					"Cannot " + action + " payment method in status " + status
							+ ". paymentMethodUuid=" + paymentMethodUuid.value()
			);
		}
	}

	private static void validateDefaultStatusCombination(boolean defaultMethod, PaymentMethodStatus status) {
		if (defaultMethod && status != PaymentMethodStatus.ACTIVE) {
			throw new InvalidPaymentMethodStateException(
					"Only ACTIVE payment methods can be default. status=" + status
			);
		}
	}

	private PaymentMethod copy(boolean nextDefaultMethod, PaymentMethodStatus nextStatus) {
		return new PaymentMethod(
				paymentMethodId,
				paymentMethodUuid,
				memberUuid,
				billingKey,
				cardName,
				fourCardNumber,
				nextDefaultMethod,
				nextStatus,
				registeredAt
		);
	}
}
