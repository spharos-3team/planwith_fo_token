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

class TokenPolicyAndReconstructorTest {

	private static final MemberUuid MEMBER = MemberUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

	@Test
	void grantTokenTypeMapping() {
		assertThat(TokenPolicy.tokenTypeOfGrant(TransactionType.CHARGE, ReferenceType.PAYMENT))
				.isEqualTo(TokenType.PAID);
		assertThat(TokenPolicy.tokenTypeOfGrant(TransactionType.REWARD, ReferenceType.GRADE_REWARD))
				.isEqualTo(TokenType.FREE);
		assertThat(TokenPolicy.tokenTypeOfGrant(TransactionType.REWARD, null))
				.isEqualTo(TokenType.BONUS);
	}

	@Test
	void reconstructsWalletFromLedgerHistory() {
		Instant now = Instant.parse("2026-08-20T00:00:00Z");
		TokenWallet live = TokenWallet.empty(MEMBER);
		TokenLedger paid = live.grant(TransactionType.CHARGE, ReferenceType.PAYMENT, 100L, "paid", now);
		TokenLedger free = live.grant(TransactionType.REWARD, ReferenceType.GRADE_REWARD, 10L, "free", now.plusSeconds(1));
		TokenLedger used = live.use(15L, ReferenceType.AI_SCHEDULE, "use", now.plusSeconds(2));

		TokenWallet reconstructed = TokenWalletReconstructor.reconstruct(MEMBER, List.of(paid, free, used));

		assertThat(reconstructed.getPaidBalance()).isEqualTo(live.getPaidBalance()).isEqualTo(95L);
		assertThat(reconstructed.getFreeBalance()).isZero();
		assertThat(reconstructed.getBonusBalance()).isZero();
		assertThat(reconstructed.getTotalBalance()).isEqualTo(95L);
	}
}
