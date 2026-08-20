package com.planwith.planwith_fo_token.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_token.domain.exception.InsufficientTokenBalanceException;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.service.TokenPolicy;

class TokenWalletTest {

	private static final MemberUuid MEMBER = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private static final Instant NOW = Instant.parse("2026-08-20T00:00:00Z");

	@Test
	void getTotalBalanceIsPaidPlusFreePlusBonus() {
		TokenWallet wallet = TokenWallet.restore(MEMBER, 100L, 20L, 10L);

		assertThat(wallet.getTotalBalance()).isEqualTo(130L);
		assertThat(wallet.getTotalBalance()).isEqualTo(
				TokenPolicy.totalBalance(wallet.getPaidBalance(), wallet.getFreeBalance(), wallet.getBonusBalance())
		);
	}

	@Test
	void grantIncreasesBalanceByTokenType() {
		TokenWallet wallet = TokenWallet.empty(MEMBER);

		TokenLedger paid = wallet.grant(TransactionType.CHARGE, ReferenceType.PAYMENT, 100L, "paid", NOW);
		TokenLedger free = wallet.grant(TransactionType.REWARD, ReferenceType.GRADE_REWARD, 10L, "free", NOW);
		TokenLedger bonus = wallet.grant(TransactionType.REWARD, null, 5L, "bonus", NOW);

		assertThat(paid.tokenType()).isEqualTo(TokenType.PAID);
		assertThat(free.tokenType()).isEqualTo(TokenType.FREE);
		assertThat(bonus.tokenType()).isEqualTo(TokenType.BONUS);
		assertThat(wallet.getPaidBalance()).isEqualTo(100L);
		assertThat(wallet.getFreeBalance()).isEqualTo(10L);
		assertThat(wallet.getBonusBalance()).isEqualTo(5L);
		assertThat(wallet.getTotalBalance()).isEqualTo(115L);
	}

	@Test
	void useDecreasesFreeThenBonusThenPaid() {
		TokenWallet wallet = TokenWallet.restore(MEMBER, 100L, 10L, 20L);

		TokenLedger used = wallet.use(25L, ReferenceType.AI_SCHEDULE, "use", NOW);

		assertThat(used.transactionType()).isEqualTo(TransactionType.USE);
		assertThat(used.amount()).isEqualTo(25L);
		assertThat(used.balanceAfter()).isEqualTo(wallet.getTotalBalance());
		assertThat(used.referenceType()).isEqualTo(ReferenceType.AI_SCHEDULE);
		assertThat(wallet.getFreeBalance()).isZero();
		assertThat(wallet.getBonusBalance()).isEqualTo(5L);
		assertThat(wallet.getPaidBalance()).isEqualTo(100L);
		assertThat(wallet.getTotalBalance()).isEqualTo(105L);
		assertThat(wallet.getTotalBalance()).isEqualTo(
				TokenPolicy.totalBalance(wallet.getPaidBalance(), wallet.getFreeBalance(), wallet.getBonusBalance())
		);
	}

	@Test
	void useRejectsNonPositiveAmount() {
		TokenWallet wallet = TokenWallet.restore(MEMBER, 10L, 0L, 0L);

		assertThatThrownBy(() -> wallet.use(0L, ReferenceType.PDF_DOWNLOAD, "use", NOW))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("positive");
	}

	@Test
	void useThrowsWhenBalanceIsInsufficient() {
		TokenWallet wallet = TokenWallet.empty(MEMBER);

		assertThatThrownBy(() -> wallet.use(1L, ReferenceType.AI_SCHEDULE, "use", NOW))
				.isInstanceOf(InsufficientTokenBalanceException.class);
	}

	@Test
	void expireClearsOnlyFreeTokens() {
		TokenWallet wallet = TokenWallet.restore(MEMBER, 30L, 15L, 8L);

		TokenLedger expired = wallet.expire(NOW);

		assertThat(expired.transactionType()).isEqualTo(TransactionType.EXPIRE);
		assertThat(expired.tokenType()).isEqualTo(TokenType.FREE);
		assertThat(expired.amount()).isEqualTo(15L);
		assertThat(wallet.getFreeBalance()).isZero();
		assertThat(wallet.getPaidBalance()).isEqualTo(30L);
		assertThat(wallet.getBonusBalance()).isEqualTo(8L);
		assertThat(TokenPolicy.expiresBeforeMonthlyGrant(TokenType.FREE)).isTrue();
		assertThat(TokenPolicy.bonusExpiresAutomatically()).isFalse();
	}

	@Test
	void negativeBalanceIsRejected() {
		assertThatThrownBy(() -> TokenWallet.restore(MEMBER, -1L, 0L, 0L))
				.isInstanceOf(IllegalArgumentException.class);
		assertThat(TokenPolicy.allowsNegativeBalance()).isFalse();
		assertThat(TokenPolicy.ledgerMutable()).isFalse();
	}
}
