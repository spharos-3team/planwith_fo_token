package com.planwith.planwith_fo_token.domain.service;

import java.util.List;

import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenType;
import com.planwith.planwith_fo_token.domain.model.TransactionType;

/**
 * 토큰 보관·사용 정책. Domain 메서드(grant/use/expire)의 기준이다.
 */
public final class TokenPolicy {

	public static final List<TokenType> DEDUCTION_ORDER = List.of(
			TokenType.FREE,
			TokenType.BONUS,
			TokenType.PAID
	);

	private TokenPolicy() {
	}

	public static long totalBalance(long paid, long free, long bonus) {
		return paid + free + bonus;
	}

	public static boolean allowsNegativeBalance() {
		return false;
	}

	public static boolean ledgerMutable() {
		return false;
	}

	public static boolean expiresBeforeMonthlyGrant(TokenType tokenType) {
		return tokenType == TokenType.FREE;
	}

	public static boolean bonusExpiresAutomatically() {
		return false;
	}

	public static TokenType tokenTypeOfGrant(TransactionType transactionType, ReferenceType referenceType) {
		if (transactionType == TransactionType.CHARGE) {
			return TokenType.PAID;
		}
		if (transactionType == TransactionType.REWARD && referenceType == ReferenceType.GRADE_REWARD) {
			return TokenType.FREE;
		}
		if (transactionType == TransactionType.REWARD) {
			return TokenType.BONUS;
		}
		throw new IllegalArgumentException(
				"Grant token type is defined only for CHARGE/REWARD. type=" + transactionType
		);
	}
}
