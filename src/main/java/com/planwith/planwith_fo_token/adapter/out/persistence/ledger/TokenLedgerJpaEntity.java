package com.planwith.planwith_fo_token.adapter.out.persistence.ledger;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TransactionType;

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
		name = "token_wallet",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_token_ledger_uuid",
				columnNames = {"token_ledger_uuid"}
		)
)
class TokenLedgerJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "token_ledger_id")
	private Long tokenLedgerId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "token_ledger_uuid", nullable = false, length = 36)
	private UUID tokenLedgerUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "transaction_type", nullable = false, length = 20)
	private TransactionType transactionType;

	@Column(name = "amount", nullable = false)
	private long amount;

	@Column(name = "balance_after", nullable = false)
	private long balanceAfter;

	@Enumerated(EnumType.STRING)
	@Column(name = "reference_type", length = 30)
	private ReferenceType referenceType;

	@Column(name = "description", length = 500)
	private String description;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected TokenLedgerJpaEntity() {
	}

	static TokenLedgerJpaEntity create(
			UUID tokenLedgerUuid,
			UUID memberUuid,
			TransactionType transactionType,
			long amount,
			long balanceAfter,
			ReferenceType referenceType,
			String description,
			Instant occurredAt,
			Instant createdAt
	) {
		TokenLedgerJpaEntity entity = new TokenLedgerJpaEntity();
		entity.tokenLedgerUuid = tokenLedgerUuid;
		entity.memberUuid = memberUuid;
		entity.transactionType = transactionType;
		entity.amount = amount;
		entity.balanceAfter = balanceAfter;
		entity.referenceType = referenceType;
		entity.description = description;
		entity.occurredAt = occurredAt;
		entity.createdAt = createdAt;
		return entity;
	}

	Long getTokenLedgerId() {
		return tokenLedgerId;
	}

	UUID getTokenLedgerUuid() {
		return tokenLedgerUuid;
	}

	UUID getMemberUuid() {
		return memberUuid;
	}

	TransactionType getTransactionType() {
		return transactionType;
	}

	long getAmount() {
		return amount;
	}

	long getBalanceAfter() {
		return balanceAfter;
	}

	ReferenceType getReferenceType() {
		return referenceType;
	}

	String getDescription() {
		return description;
	}

	Instant getOccurredAt() {
		return occurredAt;
	}

	Instant getCreatedAt() {
		return createdAt;
	}
}
