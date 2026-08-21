package com.planwith.planwith_fo_token.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_token.domain.exception.InsufficientTokenBalanceException;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.service.TokenPolicy;

/**
 * Domain 최종 체크리스트: FREE/PAID/BONUS 지급·차감·부족·우선순위·만료·총잔액.
 */
class FinalTokenDomainChecklistTest {

	private static final MemberUuid MEMBER = MemberUuid.from("e2222222-2222-2222-2222-222222222222");
	private static final Instant NOW = Instant.parse("2026-08-21T03:00:00Z");

	@Test
	void coversGrantDeductInsufficientPriorityExpireAndTotalBalance() {
		TokenWallet wallet = TokenWallet.empty(MEMBER);

		TokenLedger paid = wallet.grant(TransactionType.CHARGE, ReferenceType.PAYMENT, 100L, "paid", NOW);
		TokenLedger free = wallet.grant(TransactionType.REWARD, ReferenceType.GRADE_REWARD, 20L, "free", NOW);
		TokenLedger bonus = wallet.grant(TransactionType.REWARD, null, 10L, "bonus", NOW);

		assertThat(paid.tokenType()).isEqualTo(TokenType.PAID);
		assertThat(free.tokenType()).isEqualTo(TokenType.FREE);
		assertThat(bonus.tokenType()).isEqualTo(TokenType.BONUS);
		assertThat(wallet.getTotalBalance()).isEqualTo(130L);
		assertThat(wallet.getTotalBalance()).isEqualTo(
				TokenPolicy.totalBalance(wallet.getPaidBalance(), wallet.getFreeBalance(), wallet.getBonusBalance())
		);

		wallet.use(25L, ReferenceType.AI_SCHEDULE, "use", NOW.plusSeconds(1));
		assertThat(wallet.getFreeBalance()).isZero();
		assertThat(wallet.getBonusBalance()).isEqualTo(5L);
		assertThat(wallet.getPaidBalance()).isEqualTo(100L);
		assertThat(TokenPolicy.DEDUCTION_ORDER).containsExactly(TokenType.FREE, TokenType.BONUS, TokenType.PAID);

		assertThatThrownBy(() -> wallet.use(200L, ReferenceType.AI_SCHEDULE, "over", NOW.plusSeconds(2)))
				.isInstanceOf(InsufficientTokenBalanceException.class);

		TokenLedger expired = wallet.expire(NOW.plusSeconds(3));
		assertThat(expired.transactionType()).isEqualTo(TransactionType.EXPIRE);
		assertThat(expired.amount()).isZero();
		assertThat(wallet.getFreeBalance()).isZero();
		assertThat(wallet.getBonusBalance()).isEqualTo(5L);
		assertThat(TokenPolicy.bonusExpiresAutomatically()).isFalse();
		assertThat(TokenPolicy.expiresBeforeMonthlyGrant(TokenType.FREE)).isTrue();
	}
}
