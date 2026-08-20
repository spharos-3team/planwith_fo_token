package com.planwith.planwith_fo_token.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

public final class TokenLedger {

	private final Long tokenLedgerId;
	private final TransactionUuid transactionUuid;
	private final MemberUuid memberUuid;
	private final TransactionType transactionType;
	private final TokenType tokenType;
	private final long amount;
	private final long balanceAfter;
	private final ReferenceType referenceType;
	private final String description;
	private final Instant occurredAt;
	private final Instant createdAt;

	private TokenLedger(
			Long tokenLedgerId,
			TransactionUuid transactionUuid,
			MemberUuid memberUuid,
			TransactionType transactionType,
			TokenType tokenType,
			long amount,
			long balanceAfter,
			ReferenceType referenceType,
			String description,
			Instant occurredAt,
			Instant createdAt
	) {
		if (amount < 0) {
			throw new IllegalArgumentException("Ledger amount must be non-negative.");
		}
		this.tokenLedgerId = tokenLedgerId;
		this.transactionUuid = Objects.requireNonNull(transactionUuid, "Transaction UUID is required.");
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.transactionType = Objects.requireNonNull(transactionType, "Transaction type is required.");
		this.tokenType = tokenType;
		this.amount = amount;
		this.balanceAfter = balanceAfter;
		this.referenceType = referenceType;
		this.description = description;
		this.occurredAt = Objects.requireNonNull(occurredAt, "Occurred at is required.");
		this.createdAt = Objects.requireNonNull(createdAt, "Created at is required.");
	}

	public static TokenLedger append(
			MemberUuid memberUuid,
			TransactionType transactionType,
			TokenType tokenType,
			long amount,
			long balanceAfter,
			ReferenceType referenceType,
			String description,
			Instant occurredAt
	) {
		Instant now = occurredAt != null ? occurredAt : Instant.now();
		return new TokenLedger(
				null,
				new TransactionUuid(UUID.randomUUID()),
				memberUuid,
				transactionType,
				tokenType,
				amount,
				balanceAfter,
				referenceType,
				description,
				now,
				now
		);
	}

	public static TokenLedger restore(
			Long tokenLedgerId,
			TransactionUuid transactionUuid,
			MemberUuid memberUuid,
			TransactionType transactionType,
			TokenType tokenType,
			long amount,
			long balanceAfter,
			ReferenceType referenceType,
			String description,
			Instant occurredAt,
			Instant createdAt
	) {
		return new TokenLedger(
				tokenLedgerId,
				transactionUuid,
				memberUuid,
				transactionType,
				tokenType,
				amount,
				balanceAfter,
				referenceType,
				description,
				occurredAt,
				createdAt
		);
	}

	public Long tokenLedgerId() {
		return tokenLedgerId;
	}

	public Long ledgerId() {
		return tokenLedgerId;
	}

	public TransactionUuid transactionUuid() {
		return transactionUuid;
	}

	public TransactionUuid tokenLedgerUuid() {
		return transactionUuid;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public TransactionType transactionType() {
		return transactionType;
	}

	public TransactionType entryType() {
		return transactionType;
	}

	public TokenType tokenType() {
		return tokenType;
	}

	public long amount() {
		return amount;
	}

	public long balanceAfter() {
		return balanceAfter;
	}

	public ReferenceType referenceType() {
		return referenceType;
	}

	public String description() {
		return description;
	}

	public Instant occurredAt() {
		return occurredAt;
	}

	public Instant createdAt() {
		return createdAt;
	}
}
