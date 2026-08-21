package com.planwith.planwith_fo_token.adapter.out.persistence.gradereward;

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
		name = "grade_monthly_token_grant",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_grade_monthly_token_grant_member_month",
				columnNames = {"member_uuid", "reward_month"}
		)
)
class GradeMonthlyTokenGrantJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "grant_id")
	private Long grantId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Column(name = "reward_month", nullable = false, length = 7)
	private String rewardMonth;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "event_uuid", nullable = false, length = 36)
	private UUID eventUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "ledger_transaction_uuid", nullable = false, length = 36)
	private UUID ledgerTransactionUuid;

	@Column(name = "token_amount", nullable = false)
	private long tokenAmount;

	@Column(name = "grade_code", length = 30)
	private String gradeCode;

	@Column(name = "granted_at", nullable = false)
	private Instant grantedAt;

	protected GradeMonthlyTokenGrantJpaEntity() {
	}

	static GradeMonthlyTokenGrantJpaEntity create(
			UUID memberUuid,
			String rewardMonth,
			UUID eventUuid,
			UUID ledgerTransactionUuid,
			long tokenAmount,
			String gradeCode,
			Instant grantedAt
	) {
		GradeMonthlyTokenGrantJpaEntity entity = new GradeMonthlyTokenGrantJpaEntity();
		entity.memberUuid = memberUuid;
		entity.rewardMonth = rewardMonth;
		entity.eventUuid = eventUuid;
		entity.ledgerTransactionUuid = ledgerTransactionUuid;
		entity.tokenAmount = tokenAmount;
		entity.gradeCode = gradeCode;
		entity.grantedAt = grantedAt;
		return entity;
	}
}
