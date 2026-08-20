package com.planwith.planwith_fo_token.adapter.out.persistence.ledger;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntryType;

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
		name = "token_ledger",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_token_ledger_transaction_uuid",
				columnNames = {"transaction_uuid"}
		)
)
class TokenLedgerJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ledger_id")
	private Long ledgerId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "transaction_uuid", nullable = false, length = 36)
	private UUID transactionUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "entry_type", nullable = false, length = 20)
	private TokenLedgerEntryType entryType;

	@Column(name = "amount", nullable = false)
	private long amount;

	@Column(name = "balance_after", nullable = false)
	private long balanceAfter;

	@Column(name = "reference_type", length = 50)
	private String referenceType;

	@Column(name = "reference_uuid", length = 36)
	private String referenceUuid;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	protected TokenLedgerJpaEntity() {
	}

	static TokenLedgerJpaEntity create(
			UUID transactionUuid,
			UUID memberUuid,
			TokenLedgerEntryType entryType,
			long amount,
			long balanceAfter,
			String referenceType,
			String referenceUuid,
			Instant occurredAt
	) {
		TokenLedgerJpaEntity entity = new TokenLedgerJpaEntity();
		entity.transactionUuid = transactionUuid;
		entity.memberUuid = memberUuid;
		entity.entryType = entryType;
		entity.amount = amount;
		entity.balanceAfter = balanceAfter;
		entity.referenceType = referenceType;
		entity.referenceUuid = referenceUuid;
		entity.occurredAt = occurredAt;
		return entity;
	}

	Long getLedgerId() {
		return ledgerId;
	}

	UUID getTransactionUuid() {
		return transactionUuid;
	}

	UUID getMemberUuid() {
		return memberUuid;
	}

	TokenLedgerEntryType getEntryType() {
		return entryType;
	}

	long getAmount() {
		return amount;
	}

	long getBalanceAfter() {
		return balanceAfter;
	}

	String getReferenceType() {
		return referenceType;
	}

	String getReferenceUuid() {
		return referenceUuid;
	}

	Instant getOccurredAt() {
		return occurredAt;
	}
}
