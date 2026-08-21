package com.planwith.planwith_fo_token.domain.service;

import java.util.List;

import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenType;
import com.planwith.planwith_fo_token.domain.model.TransactionType;

/**
 * 무료·보너스·유료 토큰 Lifecycle 정책.
 * <ul>
 *   <li>FREE: 월간 등급 지급 전에 기존 FREE를 EXPIRE ledger로 소멸한다.</li>
 *   <li>BONUS (Stage 1): 자동 만료하지 않는다. 지급만 공통 grant 경로로 처리한다.</li>
 *   <li>PAID: 만료하지 않는다.</li>
 * </ul>
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

	/**
	 * 월간 FREE 신규 지급 전에 기존 FREE를 Ledger EXPIRE로 소멸할지 여부.
	 */
	public static boolean expiresBeforeMonthlyGrant(TokenType tokenType) {
		return tokenType == TokenType.FREE;
	}

	/**
	 * Stage 1: BONUS는 자동 만료하지 않는다.
	 */
	public static boolean bonusExpiresAutomatically() {
		return false;
	}

	public static boolean shouldAutoExpire(TokenType tokenType) {
		if (tokenType == TokenType.FREE) {
			return true;
		}
		if (tokenType == TokenType.BONUS) {
			return bonusExpiresAutomatically();
		}
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
