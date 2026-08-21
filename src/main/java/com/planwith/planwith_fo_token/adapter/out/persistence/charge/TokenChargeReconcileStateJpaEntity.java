package com.planwith.planwith_fo_token.adapter.out.persistence.charge;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "token_charge_reconcile_state",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_token_charge_reconcile_charge_uuid",
				columnNames = {"charge_uuid"}
		)
)
class TokenChargeReconcileStateJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reconcile_id")
	private Long reconcileId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "charge_uuid", nullable = false, length = 36)
	private UUID chargeUuid;

	@Column(name = "retry_count", nullable = false)
	private int retryCount;

	@Column(name = "last_result", length = 50)
	private String lastResult;

	@Column(name = "last_attempt_at")
	private Instant lastAttemptAt;

	@Column(name = "next_retry_at")
	private Instant nextRetryAt;

	protected TokenChargeReconcileStateJpaEntity() {
	}

	static TokenChargeReconcileStateJpaEntity create(UUID chargeUuid) {
		TokenChargeReconcileStateJpaEntity entity = new TokenChargeReconcileStateJpaEntity();
		entity.chargeUuid = chargeUuid;
		entity.retryCount = 0;
		return entity;
	}

	void recordAttempt(String result, Instant attemptedAt, Instant nextRetryAt) {
		this.retryCount++;
		this.lastResult = result;
		this.lastAttemptAt = attemptedAt;
		this.nextRetryAt = nextRetryAt;
	}

	void markSucceeded(Instant attemptedAt) {
		this.lastResult = "SUCCEEDED";
		this.lastAttemptAt = attemptedAt;
		this.nextRetryAt = null;
	}

	UUID chargeUuid() {
		return chargeUuid;
	}

	int retryCount() {
		return retryCount;
	}

	String lastResult() {
		return lastResult;
	}

	Instant nextRetryAt() {
		return nextRetryAt;
	}
}
