package com.planwith.planwith_fo_token.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.TokenBalanceResult;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TokenBalanceQueryServiceIntegrationTest {

	private static final MemberUuid NEW_MEMBER = MemberUuid.from("77777777-7777-7777-7777-777777777777");

	@Autowired
	private GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase;

	@Test
	void returnsEmptyWalletForMemberWithoutLedgerHistory() {
		TokenBalanceResult result = getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(NEW_MEMBER));

		assertThat(result.memberUuid()).isEqualTo(NEW_MEMBER);
		assertThat(result.totalBalance()).isZero();
		assertThat(result.paidBalance()).isZero();
		assertThat(result.freeBalance()).isZero();
		assertThat(result.bonusBalance()).isZero();
	}
}
