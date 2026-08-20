package com.planwith.planwith_fo_token.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

public final class TokenLedgerEntry {

	private final Long tokenLedgerId;
	private final TransactionUuid tokenLedgerUuid;
	private final MemberUuid memberUuid;
	private final TokenLedgerEntryType transactionType;
	private final long amount;
	private final long balanceAfter;
	private final TokenReferenceType referenceType;
	private final String description;
	private final Instant occurredAt;
	private final Instant createdAt;

	private TokenLedgerEntry(
			Long tokenLedgerId,
			TransactionUuid tokenLedgerUuid,
			MemberUuid memberUuid,
			TokenLedgerEntryType transactionType,
			long amount,
			long balanceAfter,
			TokenReferenceType referenceType,
			String description,
			Instant occurredAt,
			Instant createdAt
	) {
		if (amount < 0) {
			throw new IllegalArgumentException("Ledger amount must be non-negative.");
		}
		this.tokenLedgerId = tokenLedgerId;
		this.tokenLedgerUuid = Objects.requireNonNull(tokenLedgerUuid, "Token ledger UUID is required.");
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.transactionType = Objects.requireNonNull(transactionType, "Transaction type is required.");
		this.amount = amount;
		this.balanceAfter = balanceAfter;
		this.referenceType = referenceType;
		this.description = description;
		this.occurredAt = Objects.requireNonNull(occurredAt, "Occurred at is required.");
		this.createdAt = Objects.requireNonNull(createdAt, "Created at is required.");
	}

	public static TokenLedgerEntry append(
			MemberUuid memberUuid,
			TokenLedgerEntryType transactionType,
			long amount,
			long balanceAfter,
			TokenReferenceType referenceType,
			String description,
			Instant occurredAt
	) {
		Instant now = occurredAt != null ? occurredAt : Instant.now();
		return new TokenLedgerEntry(
				null,
				new TransactionUuid(UUID.randomUUID()),
				memberUuid,
				transactionType,
				amount,
				balanceAfter,
				referenceType,
				description,
				now,
				now
		);
	}

	public static TokenLedgerEntry restore(
			Long tokenLedgerId,
			TransactionUuid tokenLedgerUuid,
			MemberUuid memberUuid,
			TokenLedgerEntryType transactionType,
			long amount,
			long balanceAfter,
			TokenReferenceType referenceType,
			String description,
			Instant occurredAt,
			Instant createdAt
	) {
		return new TokenLedgerEntry(
				tokenLedgerId,
				tokenLedgerUuid,
				memberUuid,
				transactionType,
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

	public TransactionUuid tokenLedgerUuid() {
		return tokenLedgerUuid;
	}

	public TransactionUuid transactionUuid() {
		return tokenLedgerUuid;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public TokenLedgerEntryType transactionType() {
		return transactionType;
	}

	public TokenLedgerEntryType entryType() {
		return transactionType;
	}

	public long amount() {
		return amount;
	}

	public long balanceAfter() {
		return balanceAfter;
	}

	public TokenReferenceType referenceType() {
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
