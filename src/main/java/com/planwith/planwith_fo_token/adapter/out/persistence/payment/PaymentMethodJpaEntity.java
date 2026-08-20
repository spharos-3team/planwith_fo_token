package com.planwith.planwith_fo_token.adapter.out.persistence.payment;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.planwith.planwith_fo_token.domain.model.PaymentMethodStatus;

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
		name = "payment_method",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_payment_method_uuid",
				columnNames = {"payment_method_uuid"}
		)
)
class PaymentMethodJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_method_id")
	private Long paymentMethodId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "payment_method_uuid", nullable = false, length = 36)
	private UUID paymentMethodUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Column(name = "billing_key", nullable = false, length = 255)
	private String billingKey;

	@Column(name = "card_name", length = 100)
	private String cardName;

	@Column(name = "four_card_number", length = 4)
	private String fourCardNumber;

	@Column(name = "is_default", nullable = false)
	private boolean defaultMethod;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private PaymentMethodStatus status;

	@Column(name = "registered_at", nullable = false)
	private Instant registeredAt;

	protected PaymentMethodJpaEntity() {
	}

	static PaymentMethodJpaEntity create(
			UUID paymentMethodUuid,
			UUID memberUuid,
			String billingKey,
			String cardName,
			String fourCardNumber,
			boolean defaultMethod,
			PaymentMethodStatus status,
			Instant registeredAt
	) {
		PaymentMethodJpaEntity entity = new PaymentMethodJpaEntity();
		entity.paymentMethodUuid = paymentMethodUuid;
		entity.memberUuid = memberUuid;
		entity.billingKey = billingKey;
		entity.cardName = cardName;
		entity.fourCardNumber = fourCardNumber;
		entity.defaultMethod = defaultMethod;
		entity.status = status;
		entity.registeredAt = registeredAt;
		return entity;
	}

	Long getPaymentMethodId() {
		return paymentMethodId;
	}

	UUID getPaymentMethodUuid() {
		return paymentMethodUuid;
	}

	UUID getMemberUuid() {
		return memberUuid;
	}

	String getBillingKey() {
		return billingKey;
	}

	String getCardName() {
		return cardName;
	}

	String getFourCardNumber() {
		return fourCardNumber;
	}

	boolean isDefaultMethod() {
		return defaultMethod;
	}

	PaymentMethodStatus getStatus() {
		return status;
	}

	Instant getRegisteredAt() {
		return registeredAt;
	}
}
