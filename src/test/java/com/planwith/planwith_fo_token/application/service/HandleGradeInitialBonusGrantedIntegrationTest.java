package com.planwith.planwith_fo_token.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.HandleGradeInitialBonusGrantedCommand;
import com.planwith.planwith_fo_token.application.port.in.HandleGradeInitialBonusGrantedUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenLedgerQueryUseCase;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.GetTokenLedgerQuery;
import com.planwith.planwith_fo_token.application.query.TokenLedgerEntryResult;
import com.planwith.planwith_fo_token.domain.model.TokenType;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HandleGradeInitialBonusGrantedIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("f1818181-1818-1818-1818-181818181818");

	@Autowired
	private HandleGradeInitialBonusGrantedUseCase handleGradeInitialBonusGrantedUseCase;

	@Autowired
	private GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase;

	@Autowired
	private GetTokenLedgerQueryUseCase getTokenLedgerQueryUseCase;

	@Test
	void grantsInitialGradeRewardAsBonusToken() {
		handleGradeInitialBonusGrantedUseCase.handle(command(
				"f1111111-1111-1111-1111-111111111111",
				15L
		));

		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).bonusBalance())
				.isEqualTo(15L);
		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).freeBalance())
				.isZero();
		List<TokenLedgerEntryResult> ledger = getTokenLedgerQueryUseCase.getLedger(
				new GetTokenLedgerQuery(MEMBER, null, 0, 10)
		);
		assertThat(ledger).hasSize(1);
		assertThat(ledger.get(0).tokenType()).isEqualTo(TokenType.BONUS);
	}

	@Test
	void grantsOnlyOncePerMemberEvenWhenDifferentEventsArrive() {
		handleGradeInitialBonusGrantedUseCase.handle(command(
				"f2222222-2222-2222-2222-222222222222",
				10L
		));
		handleGradeInitialBonusGrantedUseCase.handle(command(
				"f3333333-3333-3333-3333-333333333333",
				20L
		));

		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).bonusBalance())
				.isEqualTo(10L);
		assertThat(getTokenLedgerQueryUseCase.getLedger(new GetTokenLedgerQuery(MEMBER, null, 0, 10)))
				.hasSize(1);
	}

	@Test
	void ignoresDuplicateEventUuid() {
		HandleGradeInitialBonusGrantedCommand command = command(
				"f5555555-5555-5555-5555-555555555555",
				30L
		);

		handleGradeInitialBonusGrantedUseCase.handle(command);
		handleGradeInitialBonusGrantedUseCase.handle(command);

		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).bonusBalance())
				.isEqualTo(30L);
		assertThat(getTokenLedgerQueryUseCase.getLedger(new GetTokenLedgerQuery(MEMBER, null, 0, 10)))
				.hasSize(1);
	}

	private static HandleGradeInitialBonusGrantedCommand command(String eventUuid, long tokenAmount) {
		return new HandleGradeInitialBonusGrantedCommand(
				UUID.fromString(eventUuid),
				MEMBER,
				tokenAmount,
				"BRONZE",
				Instant.parse("2026-08-26T00:00:00Z")
		);
	}
}
