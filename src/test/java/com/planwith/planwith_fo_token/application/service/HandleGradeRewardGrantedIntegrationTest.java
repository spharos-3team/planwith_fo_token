package com.planwith.planwith_fo_token.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.HandleGradeRewardGrantedCommand;
import com.planwith.planwith_fo_token.application.port.in.HandleGradeRewardGrantedUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenLedgerQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.GradeMonthlyTokenGrantPort;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.GetTokenLedgerQuery;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HandleGradeRewardGrantedIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("e1818181-1818-1818-1818-181818181818");

	@Autowired
	private HandleGradeRewardGrantedUseCase handleGradeRewardGrantedUseCase;

	@Autowired
	private GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase;

	@Autowired
	private GetTokenLedgerQueryUseCase getTokenLedgerQueryUseCase;

	@Autowired
	private GradeMonthlyTokenGrantPort gradeMonthlyTokenGrantPort;

	@Test
	void grantsFreeTokenOncePerMemberAndMonthEvenWithDifferentEventUuid() {
		handleGradeRewardGrantedUseCase.handle(new HandleGradeRewardGrantedCommand(
				UUID.fromString("e1111111-1111-1111-1111-111111111111"),
				MEMBER,
				20L,
				"MONTHLY_FREE_TOKEN",
				"2026-08",
				"GOLD",
				Instant.parse("2026-08-01T00:00:00Z")
		));
		handleGradeRewardGrantedUseCase.handle(new HandleGradeRewardGrantedCommand(
				UUID.fromString("e2222222-2222-2222-2222-222222222222"),
				MEMBER,
				20L,
				"MONTHLY_FREE_TOKEN",
				"2026-08",
				"GOLD",
				Instant.parse("2026-08-01T01:00:00Z")
		));

		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).freeBalance())
				.isEqualTo(20L);
		assertThat(getTokenLedgerQueryUseCase.getLedger(new GetTokenLedgerQuery(MEMBER, null, 0, 10)))
				.hasSize(1);
		assertThat(gradeMonthlyTokenGrantPort.existsByMemberUuidAndRewardMonth(MEMBER, "2026-08")).isTrue();
	}

	@Test
	void differentMonthsCanGrantSeparately() {
		handleGradeRewardGrantedUseCase.handle(new HandleGradeRewardGrantedCommand(
				UUID.fromString("e3333333-3333-3333-3333-333333333333"),
				MEMBER,
				10L,
				"MONTHLY_FREE_TOKEN",
				"2026-07",
				"SILVER",
				Instant.parse("2026-07-01T00:00:00Z")
		));
		handleGradeRewardGrantedUseCase.handle(new HandleGradeRewardGrantedCommand(
				UUID.fromString("e4444444-4444-4444-4444-444444444444"),
				MEMBER,
				15L,
				"MONTHLY_FREE_TOKEN",
				"2026-08",
				"GOLD",
				Instant.parse("2026-08-01T00:00:00Z")
		));

		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).freeBalance())
				.isEqualTo(25L);
		assertThat(getTokenLedgerQueryUseCase.getLedger(new GetTokenLedgerQuery(MEMBER, null, 0, 10)))
				.hasSize(2);
	}
}
