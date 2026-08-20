package com.planwith.planwith_fo_token.domain.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.planwith.planwith_fo_token.domain.exception.InsufficientTokenBalanceException;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.service.TokenPolicy;

public final class TokenWallet {

	private final MemberUuid memberUuid;
	private final Map<TokenKind, Long> remaining;

	private TokenWallet(MemberUuid memberUuid, Map<TokenKind, Long> remaining) {
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.remaining = remaining;
		assertNonNegative();
	}

	public static TokenWallet empty(MemberUuid memberUuid) {
		Map<TokenKind, Long> remaining = new EnumMap<>(TokenKind.class);
		for (TokenKind kind : TokenKind.values()) {
			remaining.put(kind, 0L);
		}
		return new TokenWallet(memberUuid, remaining);
	}

	public static TokenWallet restore(MemberUuid memberUuid, long paid, long free, long bonus) {
		Map<TokenKind, Long> remaining = new EnumMap<>(TokenKind.class);
		remaining.put(TokenKind.PAID, paid);
		remaining.put(TokenKind.FREE, free);
		remaining.put(TokenKind.BONUS, bonus);
		return new TokenWallet(memberUuid, remaining);
	}

	public void credit(TokenKind kind, long amount) {
		validatePositiveAmount(amount);
		remaining.merge(kind, amount, Long::sum);
	}

	public List<TokenKindDeduction> debit(long amount) {
		validatePositiveAmount(amount);
		if (totalBalance() < amount) {
			throw new InsufficientTokenBalanceException(
					"Insufficient token balance for memberUuid=" + memberUuid
			);
		}
		long leftover = amount;
		List<TokenKindDeduction> deductions = new ArrayList<>();
		for (TokenKind kind : TokenPolicy.DEDUCTION_ORDER) {
			if (leftover == 0) {
				break;
			}
			long available = remaining.getOrDefault(kind, 0L);
			long used = Math.min(available, leftover);
			if (used > 0) {
				remaining.put(kind, available - used);
				deductions.add(new TokenKindDeduction(kind, used));
				leftover -= used;
			}
		}
		return List.copyOf(deductions);
	}

	public long expireFree() {
		long expired = remaining.getOrDefault(TokenKind.FREE, 0L);
		remaining.put(TokenKind.FREE, 0L);
		return expired;
	}

	public long remainingOf(TokenKind kind) {
		return remaining.getOrDefault(kind, 0L);
	}

	public long paidBalance() {
		return remainingOf(TokenKind.PAID);
	}

	public long freeBalance() {
		return remainingOf(TokenKind.FREE);
	}

	public long bonusBalance() {
		return remainingOf(TokenKind.BONUS);
	}

	public long totalBalance() {
		return TokenPolicy.totalBalance(paidBalance(), freeBalance(), bonusBalance());
	}

	public long balance() {
		return totalBalance();
	}

	private void assertNonNegative() {
		if (TokenPolicy.allowsNegativeBalance()) {
			return;
		}
		for (TokenKind kind : TokenKind.values()) {
			if (remaining.getOrDefault(kind, 0L) < 0) {
				throw new IllegalArgumentException("Wallet balance cannot be negative. kind=" + kind);
			}
		}
	}

	private static void validatePositiveAmount(long amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("Token amount must be positive.");
		}
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}
}
