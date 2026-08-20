package com.planwith.planwith_fo_token.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

public final class TokenLedgerEntry {

	private final Long ledgerId;
	private final TransactionUuid transactionUuid;
	private final MemberUuid memberUuid;
	private final TokenLedgerEntryType entryType;
	private final long amount;
	private final long balanceAfter;
	private final String referenceType;
	private final String referenceUuid;
	private final Instant occurredAt;

	private TokenLedgerEntry(
			Long ledgerId,
			TransactionUuid transactionUuid,
			MemberUuid memberUuid,
			TokenLedgerEntryType entryType,
			long amount,
			long balanceAfter,
			String referenceType,
			String referenceUuid,
			Instant occurredAt
	) {
		this.ledgerId = ledgerId;
		this.transactionUuid = Objects.requireNonNull(transactionUuid, "Transaction UUID is required.");
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.entryType = Objects.requireNonNull(entryType, "Entry type is required.");
		this.amount = amount;
		this.balanceAfter = balanceAfter;
		this.referenceType = referenceType;
		this.referenceUuid = referenceUuid;
		this.occurredAt = Objects.requireNonNull(occurredAt, "Occurred at is required.");
	}

	public static TokenLedgerEntry credit(
			MemberUuid memberUuid,
			long amount,
			long balanceAfter,
			TokenLedgerEntryType entryType
	) {
		return pending(null, TransactionUuid.from(java.util.UUID.randomUUID().toString()),
				memberUuid, entryType, amount, balanceAfter, null, null, Instant.now());
	}

	public static TokenLedgerEntry debit(
			MemberUuid memberUuid,
			long amount,
			long balanceAfter,
			TokenLedgerEntryType entryType
	) {
		return pending(null, TransactionUuid.from(java.util.UUID.randomUUID().toString()),
				memberUuid, entryType, -amount, balanceAfter, null, null, Instant.now());
	}

	public static TokenLedgerEntry pending(
			Long ledgerId,
			TransactionUuid transactionUuid,
			MemberUuid memberUuid,
			TokenLedgerEntryType entryType,
			long signedAmount,
			long balanceAfter,
			String referenceType,
			String referenceUuid,
			Instant occurredAt
	) {
		return new TokenLedgerEntry(
				ledgerId,
				transactionUuid,
				memberUuid,
				entryType,
				signedAmount,
				balanceAfter,
				referenceType,
				referenceUuid,
				occurredAt
		);
	}

	public TokenLedgerEntry withLedgerId(Long ledgerId) {
		return new TokenLedgerEntry(
				ledgerId,
				transactionUuid,
				memberUuid,
				entryType,
				amount,
				balanceAfter,
				referenceType,
				referenceUuid,
				occurredAt
		);
	}

	public Long ledgerId() {
		return ledgerId;
	}

	public TransactionUuid transactionUuid() {
		return transactionUuid;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public TokenLedgerEntryType entryType() {
		return entryType;
	}

	public long amount() {
		return amount;
	}

	public long balanceAfter() {
		return balanceAfter;
	}

	public String referenceType() {
		return referenceType;
	}

	public String referenceUuid() {
		return referenceUuid;
	}

	public Instant occurredAt() {
		return occurredAt;
	}
}
