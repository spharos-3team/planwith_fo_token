package com.planwith.planwith_fo_token.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public final class PaymentMethod {

	private final Long paymentMethodId;
	private final UUID paymentMethodUuid;
	private final MemberUuid memberUuid;
	private final String billingKey;
	private final String cardName;
	private final String fourCardNumber;
	private final boolean defaultMethod;
	private final PaymentMethodStatus status;
	private final Instant registeredAt;

	private PaymentMethod(
			Long paymentMethodId,
			UUID paymentMethodUuid,
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
	}

	public static PaymentMethod restore(
			Long paymentMethodId,
			UUID paymentMethodUuid,
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

	public Long paymentMethodId() {
		return paymentMethodId;
	}

	public UUID paymentMethodUuid() {
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
}
