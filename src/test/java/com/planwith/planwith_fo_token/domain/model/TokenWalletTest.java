package com.planwith.planwith_fo_token.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_token.domain.exception.InsufficientTokenBalanceException;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

class TokenWalletTest {

	@Test
	void creditIncreasesBalance() {
		TokenWallet wallet = TokenWallet.create(MemberUuid.from("11111111-1111-1111-1111-111111111111"));

		TokenLedgerEntry entry = wallet.credit(100L, TokenLedgerEntryType.REWARD);

		assertThat(wallet.balance()).isEqualTo(100L);
		assertThat(entry.balanceAfter()).isEqualTo(100L);
		assertThat(entry.amount()).isEqualTo(100L);
	}

	@Test
	void debitDecreasesBalance() {
		TokenWallet wallet = TokenWallet.restore(
				1L,
				MemberUuid.from("11111111-1111-1111-1111-111111111111"),
				100L,
				0L
		);

		TokenLedgerEntry entry = wallet.debit(30L, TokenLedgerEntryType.USE);

		assertThat(wallet.balance()).isEqualTo(70L);
		assertThat(entry.balanceAfter()).isEqualTo(70L);
		assertThat(entry.amount()).isEqualTo(-30L);
	}

	@Test
	void debitThrowsWhenBalanceIsInsufficient() {
		TokenWallet wallet = TokenWallet.create(MemberUuid.from("11111111-1111-1111-1111-111111111111"));

		assertThatThrownBy(() -> wallet.debit(1L, TokenLedgerEntryType.USE))
				.isInstanceOf(InsufficientTokenBalanceException.class);
	}
}
