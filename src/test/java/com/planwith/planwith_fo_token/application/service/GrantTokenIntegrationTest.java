package com.planwith.planwith_fo_token.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.ChargeTokenCommand;
import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;
import com.planwith.planwith_fo_token.application.command.HandleGradeRewardGrantedCommand;
import com.planwith.planwith_fo_token.application.command.HandlePaymentCompletedCommand;
import com.planwith.planwith_fo_token.application.command.RecoverTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.HandleGradeRewardGrantedUseCase;
import com.planwith.planwith_fo_token.application.port.in.HandlePaymentCompletedUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.ChargeTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.GrantTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.RecoverTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenLedgerQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.LoadTokenLedgerPort;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.GetTokenLedgerQuery;
import com.planwith.planwith_fo_token.application.query.TokenBalanceResult;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GrantTokenIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("44444444-4444-4444-4444-444444444444");

	@Autowired
	private GrantTokenUseCase grantTokenUseCase;

	@Autowired
	private ChargeTokenUseCase chargeTokenUseCase;

	@Autowired
	private RecoverTokenUseCase recoverTokenUseCase;

	@Autowired
	private HandlePaymentCompletedUseCase handlePaymentCompletedUseCase;

	@Autowired
	private HandleGradeRewardGrantedUseCase handleGradeRewardGrantedUseCase;

	@Autowired
	private GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase;

	@Autowired
	private GetTokenLedgerQueryUseCase getTokenLedgerQueryUseCase;

	@Autowired
	private LoadTokenLedgerPort loadTokenLedgerPort;

	@Test
	void rejectsNonPositiveGrantAmount() {
		assertThatThrownBy(() -> grantTokenUseCase.grant(GrantTokenCommand.bonusReward(
				TransactionUuid.from("d1111111-1111-1111-1111-111111111111"),
				MEMBER,
				0L,
				"bonus-1",
				"invalid"
		))).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("positive");
	}

	@Test
	void paidChargeIncreasesPaidBalanceAndCreatesOutbox() {
		UUID tx = UUID.fromString("d2222222-2222-2222-2222-222222222222");
		chargeTokenUseCase.charge(new ChargeTokenCommand(
				new TransactionUuid(tx),
				MEMBER,
				50L,
				"PAYMENT",
				"pay-1",
				"paid charge"
		));

		TokenBalanceResult balance = getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER));
		assertThat(balance.paidBalance()).isEqualTo(50L);
		assertThat(loadTokenLedgerPort.findByTransactionUuid(new TransactionUuid(tx))).isPresent();
	}

	@Test
	void gradeRewardIncreasesFreeBalanceAndCreatesRewardOutbox() {
		UUID tx = UUID.fromString("d3333333-3333-3333-3333-333333333333");
		grantTokenUseCase.grant(GrantTokenCommand.gradeReward(
				new TransactionUuid(tx),
				MEMBER,
				15L,
				"GOLD",
				"grade grant"
		));

		TokenBalanceResult balance = getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER));
		assertThat(balance.freeBalance()).isEqualTo(15L);
		assertThat(loadTokenLedgerPort.findByTransactionUuid(new TransactionUuid(tx))).isPresent();
	}

	@Test
	void bonusRewardIncreasesBonusBalance() {
		UUID tx = UUID.fromString("d4444444-4444-4444-4444-444444444444");
		grantTokenUseCase.grant(GrantTokenCommand.bonusReward(
				new TransactionUuid(tx),
				MEMBER,
				25L,
				"promo-1",
				"bonus grant"
		));

		TokenBalanceResult balance = getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER));
		assertThat(balance.bonusBalance()).isEqualTo(25L);
	}

	@Test
	void duplicateTransactionUuidIsIdempotent() {
		UUID tx = UUID.fromString("d5555555-5555-5555-5555-555555555555");
		GrantTokenCommand command = GrantTokenCommand.paidCharge(
				new TransactionUuid(tx),
				MEMBER,
				40L,
				"pay-dup",
				"dup charge"
		);
		grantTokenUseCase.grant(command);
		grantTokenUseCase.grant(command);

		TokenBalanceResult balance = getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER));
		assertThat(balance.paidBalance()).isEqualTo(40L);
		assertThat(getTokenLedgerQueryUseCase.getLedger(new GetTokenLedgerQuery(MEMBER, null, 0, 10)))
				.hasSize(1);
	}

	@Test
	void recoverDelegatesToCommonGrantLogic() {
		UUID tx = UUID.fromString("d6666666-6666-6666-6666-666666666666");
		recoverTokenUseCase.recover(new RecoverTokenCommand(
				new TransactionUuid(tx),
				MEMBER,
				10L,
				"PAYMENT",
				"recover-ref",
				"recovery"
		));

		TokenBalanceResult balance = getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER));
		assertThat(balance.paidBalance()).isEqualTo(10L);
		assertThat(loadTokenLedgerPort.findByTransactionUuid(new TransactionUuid(tx))).isPresent();
	}

	@Test
	void handlePaymentCompletedGrantsTokensAndRecordsProcessedEvent() {
		UUID eventUuid = UUID.fromString("d7777777-7777-7777-7777-777777777777");
		handlePaymentCompletedUseCase.handle(new HandlePaymentCompletedCommand(
				eventUuid,
				MEMBER,
				80L,
				"payment-ref",
				java.time.Instant.parse("2026-01-01T00:00:00Z")
		));
		handlePaymentCompletedUseCase.handle(new HandlePaymentCompletedCommand(
				eventUuid,
				MEMBER,
				80L,
				"payment-ref",
				java.time.Instant.parse("2026-01-01T00:00:00Z")
		));

		TokenBalanceResult balance = getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER));
		assertThat(balance.paidBalance()).isEqualTo(80L);
		assertThat(getTokenLedgerQueryUseCase.getLedger(new GetTokenLedgerQuery(MEMBER, null, 0, 10)))
				.hasSize(1);
	}

	@Test
	void handleGradeRewardGrantedGrantsFreeTokens() {
		UUID eventUuid = UUID.fromString("d8888888-8888-8888-8888-888888888888");
		handleGradeRewardGrantedUseCase.handle(new HandleGradeRewardGrantedCommand(
				eventUuid,
				MEMBER,
				12L,
				"MONTHLY_FREE_TOKEN",
				"2026-02",
				"PLATINUM",
				java.time.Instant.parse("2026-02-01T00:00:00Z")
		));

		TokenBalanceResult balance = getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER));
		assertThat(balance.freeBalance()).isEqualTo(12L);
		assertThat(loadTokenLedgerPort.findByTransactionUuid(
				com.planwith.planwith_fo_token.domain.model.GradeMonthlyTokenGrant.ledgerTransactionUuidOf(
						MEMBER, "2026-02"
				)
		)).isPresent();
	}
}
