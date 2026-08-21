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

import com.planwith.planwith_fo_token.application.command.ExpireTokenCommand;
import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;
import com.planwith.planwith_fo_token.application.command.HandleGradeRewardGrantedCommand;
import com.planwith.planwith_fo_token.application.port.in.HandleGradeRewardGrantedUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.ExpireTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.GrantTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenLedgerQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.LoadTokenLedgerPort;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.GetTokenLedgerQuery;
import com.planwith.planwith_fo_token.application.query.TokenLedgerEntryResult;
import com.planwith.planwith_fo_token.domain.model.GradeMonthlyTokenGrant;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TokenType;
import com.planwith.planwith_fo_token.domain.model.TransactionType;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;
import com.planwith.planwith_fo_token.domain.service.TokenPolicy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FreeBonusTokenPolicyIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("f1919191-1919-1919-1919-191919191919");

	@Autowired
	private HandleGradeRewardGrantedUseCase handleGradeRewardGrantedUseCase;

	@Autowired
	private GrantTokenUseCase grantTokenUseCase;

	@Autowired
	private ExpireTokenUseCase expireTokenUseCase;

	@Autowired
	private GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase;

	@Autowired
	private GetTokenLedgerQueryUseCase getTokenLedgerQueryUseCase;

	@Autowired
	private LoadTokenLedgerPort loadTokenLedgerPort;

	@Test
	void monthlyGradeRewardExpiresPreviousFreeThenGrantsNewFree() {
		handleGradeRewardGrantedUseCase.handle(new HandleGradeRewardGrantedCommand(
				UUID.fromString("f1111111-1111-1111-1111-111111111111"),
				MEMBER,
				20L,
				"MONTHLY_FREE_TOKEN",
				"2026-08",
				"GOLD",
				Instant.parse("2026-08-01T00:00:00Z")
		));
		handleGradeRewardGrantedUseCase.handle(new HandleGradeRewardGrantedCommand(
				UUID.fromString("f2222222-2222-2222-2222-222222222222"),
				MEMBER,
				30L,
				"MONTHLY_FREE_TOKEN",
				"2026-09",
				"GOLD",
				Instant.parse("2026-09-01T00:00:00Z")
		));

		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).freeBalance())
				.isEqualTo(30L);

		List<TokenLedgerEntryResult> ledger = getTokenLedgerQueryUseCase.getLedger(
				new GetTokenLedgerQuery(MEMBER, null, 0, 20)
		);
		assertThat(ledger).hasSize(3);
		assertThat(ledger).extracting(TokenLedgerEntryResult::transactionType)
				.containsExactly(
						TransactionType.REWARD,
						TransactionType.EXPIRE,
						TransactionType.REWARD
				);

		TokenLedger expireLedger = loadTokenLedgerPort.findByTransactionUuid(
				GradeMonthlyTokenGrant.expireLedgerTransactionUuidOf(MEMBER, "2026-09")
		).orElseThrow();
		assertThat(expireLedger.tokenType()).isEqualTo(TokenType.FREE);
		assertThat(expireLedger.amount()).isEqualTo(20L);
		assertThat(expireLedger.balanceAfter()).isEqualTo(0L);
	}

	@Test
	void bonusGrantUsesCommonPathAndDoesNotAutoExpireInStage1() {
		assertThat(TokenPolicy.bonusExpiresAutomatically()).isFalse();
		assertThat(TokenPolicy.shouldAutoExpire(TokenType.BONUS)).isFalse();

		UUID bonusTx = UUID.fromString("f3333333-3333-3333-3333-333333333333");
		grantTokenUseCase.grant(GrantTokenCommand.bonusReward(
				new TransactionUuid(bonusTx),
				MEMBER,
				25L,
				"promo-stage1",
				"bonus grant"
		));

		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).bonusBalance())
				.isEqualTo(25L);

		expireTokenUseCase.expire(new ExpireTokenCommand(
				new TransactionUuid(UUID.fromString("f4444444-4444-4444-4444-444444444444")),
				MEMBER
		));

		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).bonusBalance())
				.isEqualTo(25L);
		assertThat(getTokenLedgerQueryUseCase.getLedger(new GetTokenLedgerQuery(MEMBER, null, 0, 10)))
				.hasSize(1);
	}

	@Test
	void expireRecordsLedgerAmountAndBalanceAfterWithoutRawZeroUpdate() {
		grantTokenUseCase.grant(GrantTokenCommand.gradeReward(
				new TransactionUuid(UUID.fromString("f5555555-5555-5555-5555-555555555555")),
				MEMBER,
				18L,
				"2026-08",
				"seed free"
		));
		UUID expireTx = UUID.fromString("f6666666-6666-6666-6666-666666666666");
		expireTokenUseCase.expire(new ExpireTokenCommand(new TransactionUuid(expireTx), MEMBER));

		TokenLedger expireLedger = loadTokenLedgerPort.findByTransactionUuid(new TransactionUuid(expireTx))
				.orElseThrow();
		assertThat(expireLedger.transactionType()).isEqualTo(TransactionType.EXPIRE);
		assertThat(expireLedger.amount()).isEqualTo(18L);
		assertThat(expireLedger.balanceAfter()).isZero();
		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).freeBalance())
				.isZero();
	}
}
