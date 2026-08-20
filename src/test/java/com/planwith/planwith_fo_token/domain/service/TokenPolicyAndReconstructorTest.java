package com.planwith.planwith_fo_token.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_token.domain.model.TokenKind;
import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntry;
import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntryType;
import com.planwith.planwith_fo_token.domain.model.TokenReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

class TokenPolicyAndReconstructorTest {

	private static final MemberUuid MEMBER = MemberUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

	@Test
	void grantKindMapping() {
		assertThat(TokenPolicy.kindOfGrant(TokenLedgerEntryType.CHARGE, TokenReferenceType.PAYMENT))
				.isEqualTo(TokenKind.PAID);
		assertThat(TokenPolicy.kindOfGrant(TokenLedgerEntryType.REWARD, TokenReferenceType.GRADE_REWARD))
				.isEqualTo(TokenKind.FREE);
		assertThat(TokenPolicy.kindOfGrant(TokenLedgerEntryType.REWARD, null))
				.isEqualTo(TokenKind.BONUS);
	}

	@Test
	void reconstructsWalletFromLedgerHistory() {
		Instant now = Instant.parse("2026-08-20T00:00:00Z");
		TokenWallet live = TokenWallet.empty(MEMBER);
		TokenLedgerEntry paid = TokenLedgerDomainService.grant(
				live, TokenLedgerEntryType.CHARGE, TokenReferenceType.PAYMENT, 100L, "paid", now
		);
		TokenLedgerEntry free = TokenLedgerDomainService.grant(
				live, TokenLedgerEntryType.REWARD, TokenReferenceType.GRADE_REWARD, 10L, "free", now.plusSeconds(1)
		);
		TokenLedgerEntry used = TokenLedgerDomainService.use(
				live, 15L, TokenReferenceType.AI_SCHEDULE, "use", now.plusSeconds(2)
		);

		TokenWallet reconstructed = TokenWalletReconstructor.reconstruct(MEMBER, List.of(paid, free, used));

		assertThat(reconstructed.paidBalance()).isEqualTo(live.paidBalance()).isEqualTo(95L);
		assertThat(reconstructed.freeBalance()).isZero();
		assertThat(reconstructed.bonusBalance()).isZero();
		assertThat(reconstructed.totalBalance()).isEqualTo(95L);
	}
}
