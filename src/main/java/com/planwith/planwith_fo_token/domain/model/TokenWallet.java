package com.planwith.planwith_fo_token.domain.model;

import java.util.Objects;

import com.planwith.planwith_fo_token.domain.exception.InsufficientTokenBalanceException;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public final class TokenWallet {

	private final Long walletId;
	private final MemberUuid memberUuid;
	private long balance;
	private long version;

	private TokenWallet(Long walletId, MemberUuid memberUuid, long balance, long version) {
		this.walletId = walletId;
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.balance = balance;
		this.version = version;
	}

	public static TokenWallet create(MemberUuid memberUuid) {
		return new TokenWallet(null, memberUuid, 0L, 0L);
	}

	public static TokenWallet restore(Long walletId, MemberUuid memberUuid, long balance, long version) {
		if (balance < 0) {
			throw new IllegalArgumentException("Wallet balance cannot be negative.");
		}
		return new TokenWallet(walletId, memberUuid, balance, version);
	}

	public TokenLedgerEntry credit(long amount, TokenLedgerEntryType entryType) {
		validatePositiveAmount(amount);
		balance += amount;
		return TokenLedgerEntry.credit(memberUuid, amount, balance, entryType);
	}

	public TokenLedgerEntry debit(long amount, TokenLedgerEntryType entryType) {
		validatePositiveAmount(amount);
		if (balance < amount) {
			throw new InsufficientTokenBalanceException(
					"Insufficient token balance for memberUuid=" + memberUuid
			);
		}
		balance -= amount;
		return TokenLedgerEntry.debit(memberUuid, amount, balance, entryType);
	}

	private static void validatePositiveAmount(long amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("Token amount must be positive.");
		}
	}

	public Long walletId() {
		return walletId;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public long balance() {
		return balance;
	}

	public long version() {
		return version;
	}
}
