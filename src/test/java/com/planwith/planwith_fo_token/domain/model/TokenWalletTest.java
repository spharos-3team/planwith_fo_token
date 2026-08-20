package com.planwith.planwith_fo_token.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_token.domain.exception.InsufficientTokenBalanceException;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.service.TokenPolicy;

class TokenWalletTest {

	private static final MemberUuid MEMBER = MemberUuid.from("11111111-1111-1111-1111-111111111111");

	@Test
	void totalBalanceIsPaidPlusFreePlusBonus() {
		TokenWallet wallet = TokenWallet.restore(MEMBER, 100L, 20L, 10L);

		assertThat(wallet.totalBalance()).isEqualTo(130L);
		assertThat(wallet.totalBalance()).isEqualTo(
				TokenPolicy.totalBalance(wallet.paidBalance(), wallet.freeBalance(), wallet.bonusBalance())
		);
	}

	@Test
	void debitUsesFreeThenBonusThenPaid() {
		TokenWallet wallet = TokenWallet.restore(MEMBER, 50L, 10L, 20L);

		var deductions = wallet.debit(25L);

		assertThat(deductions).containsExactly(
				new TokenKindDeduction(TokenKind.FREE, 10L),
				new TokenKindDeduction(TokenKind.BONUS, 15L)
		);
		assertThat(wallet.freeBalance()).isZero();
		assertThat(wallet.bonusBalance()).isEqualTo(5L);
		assertThat(wallet.paidBalance()).isEqualTo(50L);
	}

	@Test
	void debitThrowsWhenBalanceIsInsufficient() {
		TokenWallet wallet = TokenWallet.empty(MEMBER);

		assertThatThrownBy(() -> wallet.debit(1L))
				.isInstanceOf(InsufficientTokenBalanceException.class);
	}

	@Test
	void expireFreeClearsOnlyFreeTokens() {
		TokenWallet wallet = TokenWallet.restore(MEMBER, 30L, 15L, 8L);

		long expired = wallet.expireFree();

		assertThat(expired).isEqualTo(15L);
		assertThat(wallet.freeBalance()).isZero();
		assertThat(wallet.paidBalance()).isEqualTo(30L);
		assertThat(wallet.bonusBalance()).isEqualTo(8L);
		assertThat(TokenPolicy.expiresBeforeMonthlyGrant(TokenKind.FREE)).isTrue();
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
