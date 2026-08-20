package com.planwith.planwith_fo_token.domain.service;

import java.util.List;

import com.planwith.planwith_fo_token.domain.model.TokenKind;
import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntryType;
import com.planwith.planwith_fo_token.domain.model.TokenReferenceType;

/**
 * 토큰 보관·사용 정책. 이후 기능 구현의 기준이며 ERD 테이블명과 역할을 함께 고정한다.
 *
 * <ul>
 *   <li>ERD {@code token_wallet} = Ledger (append-only 거래원장). 테이블명은 ERD를 유지한다.</li>
 *   <li>Wallet = 회원별 현재 잔액(PAID/FREE/BONUS). Ledger 재실행으로 복원한다. 별도 잔액 테이블을 두지 않는다.</li>
 *   <li>{@code token_charge} = 결제 충전 요청/결과. PAID 지급의 선행 상태다.</li>
 *   <li>{@code payment_method} = 등록 카드. Charge의 BILLING_KEY 결제에 사용한다.</li>
 * </ul>
 */
public final class TokenPolicy {

	public static final List<TokenKind> DEDUCTION_ORDER = List.of(
			TokenKind.FREE,
			TokenKind.BONUS,
			TokenKind.PAID
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

	public static boolean expiresBeforeMonthlyGrant(TokenKind kind) {
		return kind == TokenKind.FREE;
	}

	public static boolean bonusExpiresAutomatically() {
		return false;
	}

	public static TokenKind kindOfGrant(TokenLedgerEntryType transactionType, TokenReferenceType referenceType) {
		if (transactionType == TokenLedgerEntryType.CHARGE) {
			return TokenKind.PAID;
		}
		if (transactionType == TokenLedgerEntryType.REWARD && referenceType == TokenReferenceType.GRADE_REWARD) {
			return TokenKind.FREE;
		}
		if (transactionType == TokenLedgerEntryType.REWARD) {
			return TokenKind.BONUS;
		}
		throw new IllegalArgumentException(
				"Grant kind is defined only for CHARGE/REWARD. type=" + transactionType
		);
	}
}
