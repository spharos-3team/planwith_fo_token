package com.planwith.planwith_fo_token.domain.model;

import java.time.Instant;
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
	private final Map<TokenType, Long> remaining;

	private TokenWallet(MemberUuid memberUuid, Map<TokenType, Long> remaining) {
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.remaining = remaining;
		assertNonNegative();
	}

	public static TokenWallet empty(MemberUuid memberUuid) {
		Map<TokenType, Long> remaining = new EnumMap<>(TokenType.class);
		for (TokenType tokenType : TokenType.values()) {
			remaining.put(tokenType, 0L);
		}
		return new TokenWallet(memberUuid, remaining);
	}

	public static TokenWallet restore(MemberUuid memberUuid, long paid, long free, long bonus) {
		Map<TokenType, Long> remaining = new EnumMap<>(TokenType.class);
		remaining.put(TokenType.PAID, paid);
		remaining.put(TokenType.FREE, free);
		remaining.put(TokenType.BONUS, bonus);
		return new TokenWallet(memberUuid, remaining);
	}

	public TokenLedger grant(
			TransactionType transactionType,
			ReferenceType referenceType,
			long amount,
			String description,
			Instant occurredAt
	) {
		TokenType tokenType = TokenPolicy.tokenTypeOfGrant(transactionType, referenceType);
		increase(tokenType, amount);
		return TokenLedger.append(
				memberUuid,
				transactionType,
				tokenType,
				amount,
				getTotalBalance(),
				referenceType,
				description,
				occurredAt
		);
	}

	public TokenLedger use(
			long amount,
			ReferenceType referenceType,
			String description,
			Instant occurredAt
	) {
		decrease(amount);
		return TokenLedger.append(
				memberUuid,
				TransactionType.USE,
				null,
				amount,
				getTotalBalance(),
				referenceType,
				description,
				occurredAt
		);
	}

	public TokenLedger expire(Instant occurredAt) {
		long expired = clearFreeBalance();
		return TokenLedger.append(
				memberUuid,
				TransactionType.EXPIRE,
				TokenType.FREE,
				expired,
				getTotalBalance(),
				ReferenceType.GRADE_REWARD,
				"FREE token monthly expiry",
				occurredAt
		);
	}

	public void apply(TokenLedger ledger) {
		TransactionType type = ledger.transactionType();
		if (type == TransactionType.CHARGE || type == TransactionType.REWARD) {
			TokenType tokenType = ledger.tokenType() != null
					? ledger.tokenType()
					: TokenPolicy.tokenTypeOfGrant(type, ledger.referenceType());
			increase(tokenType, ledger.amount());
			return;
		}
		if (type == TransactionType.USE) {
			decrease(ledger.amount());
			return;
		}
		if (type == TransactionType.EXPIRE) {
			clearFreeBalance();
		}
	}

	public List<TokenTypeDeduction> decrease(long amount) {
		validatePositiveAmount(amount);
		if (getTotalBalance() < amount) {
			throw new InsufficientTokenBalanceException(
					"Insufficient token balance for memberUuid=" + memberUuid
			);
		}
		long leftover = amount;
		List<TokenTypeDeduction> deductions = new ArrayList<>();
		for (TokenType tokenType : TokenPolicy.DEDUCTION_ORDER) {
			if (leftover == 0) {
				break;
			}
			long available = remaining.getOrDefault(tokenType, 0L);
			long used = Math.min(available, leftover);
			if (used > 0) {
				remaining.put(tokenType, available - used);
				deductions.add(new TokenTypeDeduction(tokenType, used));
				leftover -= used;
			}
		}
		return List.copyOf(deductions);
	}

	public long getTotalBalance() {
		return TokenPolicy.totalBalance(getPaidBalance(), getFreeBalance(), getBonusBalance());
	}

	public long getPaidBalance() {
		return remainingOf(TokenType.PAID);
	}

	public long getFreeBalance() {
		return remainingOf(TokenType.FREE);
	}

	public long getBonusBalance() {
		return remainingOf(TokenType.BONUS);
	}

	public long paidBalance() {
		return getPaidBalance();
	}

	public long freeBalance() {
		return getFreeBalance();
	}

	public long bonusBalance() {
		return getBonusBalance();
	}

	public long totalBalance() {
		return getTotalBalance();
	}

	public long balance() {
		return getTotalBalance();
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	private void increase(TokenType tokenType, long amount) {
		validatePositiveAmount(amount);
		remaining.merge(tokenType, amount, Long::sum);
	}

	private long clearFreeBalance() {
		long expired = remaining.getOrDefault(TokenType.FREE, 0L);
		remaining.put(TokenType.FREE, 0L);
		return expired;
	}

	private long remainingOf(TokenType tokenType) {
		return remaining.getOrDefault(tokenType, 0L);
	}

	private void assertNonNegative() {
		if (TokenPolicy.allowsNegativeBalance()) {
			return;
		}
		for (TokenType tokenType : TokenType.values()) {
			if (remaining.getOrDefault(tokenType, 0L) < 0) {
				throw new IllegalArgumentException("Wallet balance cannot be negative. tokenType=" + tokenType);
			}
		}
	}

	private static void validatePositiveAmount(long amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("Token amount must be positive.");
		}
	}
}
