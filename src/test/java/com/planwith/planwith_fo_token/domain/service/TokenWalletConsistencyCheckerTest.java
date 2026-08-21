package com.planwith.planwith_fo_token.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TokenType;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.TransactionType;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

class TokenWalletConsistencyCheckerTest {

	private static final MemberUuid MEMBER = MemberUuid.from("b2121212-2121-2121-2121-212121212121");

	@Test
	void detectsConsistentWalletAndLedgerBalanceAfter() {
		Instant now = Instant.parse("2026-08-21T00:00:00Z");
		TokenWallet wallet = TokenWallet.empty(MEMBER);
		TokenLedger charge = wallet.grant(TransactionType.CHARGE, ReferenceType.PAYMENT, 100L, "paid", now);
		TokenLedger use = wallet.use(30L, ReferenceType.AI_SCHEDULE, "use", now.plusSeconds(1));

		TokenWalletConsistencyChecker.ConsistencyResult result =
				TokenWalletConsistencyChecker.check(MEMBER, List.of(charge, use));

		assertThat(result.consistent()).isTrue();
		assertThat(result.walletTotalBalance()).isEqualTo(70L);
		assertThat(result.ledgerBalanceAfter()).isEqualTo(70L);
	}

	@Test
	void detectsInconsistencyWhenLedgerBalanceAfterDoesNotMatchReconstruction() {
		Instant now = Instant.parse("2026-08-21T00:00:00Z");
		TokenWallet wallet = TokenWallet.empty(MEMBER);
		TokenLedger charge = wallet.grant(TransactionType.CHARGE, ReferenceType.PAYMENT, 50L, "paid", now);
		TokenLedger corrupted = TokenLedger.appendWithTransactionUuid(
				charge.transactionUuid(),
				MEMBER,
				TransactionType.CHARGE,
				charge.tokenType(),
				50L,
				999L,
				ReferenceType.PAYMENT,
				"corrupted",
				now.plusSeconds(1)
		);

		TokenWalletConsistencyChecker.ConsistencyResult result =
				TokenWalletConsistencyChecker.check(MEMBER, List.of(charge, corrupted));

		assertThat(result.consistent()).isFalse();
		assertThat(result.walletTotalBalance()).isEqualTo(100L);
		assertThat(result.ledgerBalanceAfter()).isEqualTo(999L);
	}
}
