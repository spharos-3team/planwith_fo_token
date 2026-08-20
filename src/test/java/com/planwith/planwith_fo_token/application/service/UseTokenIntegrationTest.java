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
import com.planwith.planwith_fo_token.application.command.UseTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.ChargeTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.GrantTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.UseTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenLedgerQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.LoadTokenLedgerPort;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.GetTokenLedgerQuery;
import com.planwith.planwith_fo_token.application.query.TokenBalanceResult;
import com.planwith.planwith_fo_token.domain.exception.InsufficientTokenBalanceException;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;
import com.planwith.planwith_fo_token.domain.service.TokenPolicy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UseTokenIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("55555555-5555-5555-5555-555555555555");

	@Autowired
	private ChargeTokenUseCase chargeTokenUseCase;

	@Autowired
	private GrantTokenUseCase grantTokenUseCase;

	@Autowired
	private UseTokenUseCase useTokenUseCase;

	@Autowired
	private GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase;

	@Autowired
	private GetTokenLedgerQueryUseCase getTokenLedgerQueryUseCase;

	@Autowired
	private LoadTokenLedgerPort loadTokenLedgerPort;

	@Test
	void deductsFreeThenBonusThenPaidAndKeepsTotalConsistent() {
		seedBalances(100L, 10L, 20L);

		useTokenUseCase.use(new UseTokenCommand(
				TransactionUuid.from("e1111111-1111-1111-1111-111111111111"),
				MEMBER,
				25L,
				"AI_SCHEDULE",
				"schedule-1",
				"ai schedule use"
		));

		TokenBalanceResult balance = getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER));
		assertThat(balance.freeBalance()).isZero();
		assertThat(balance.bonusBalance()).isEqualTo(5L);
		assertThat(balance.paidBalance()).isEqualTo(100L);
		assertThat(balance.totalBalance()).isEqualTo(105L);
		assertThat(balance.totalBalance()).isEqualTo(
				TokenPolicy.totalBalance(balance.paidBalance(), balance.freeBalance(), balance.bonusBalance())
		);
	}

	@Test
	void rejectsNonPositiveUseAmount() {
		assertThatThrownBy(() -> useTokenUseCase.use(new UseTokenCommand(
				TransactionUuid.from("e2222222-2222-2222-2222-222222222222"),
				MEMBER,
				0L,
				"PDF_DOWNLOAD",
				"pdf-1",
				"invalid"
		))).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("positive");
	}

	@Test
	void rejectsInvalidUseReferenceType() {
		assertThatThrownBy(() -> useTokenUseCase.use(new UseTokenCommand(
				TransactionUuid.from("e3333333-3333-3333-3333-333333333333"),
				MEMBER,
				1L,
				"PAYMENT",
				"pay-1",
				"invalid ref"
		))).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("referenceType");
	}

	@Test
	void throwsWhenBalanceInsufficient() {
		seedBalances(5L, 0L, 0L);

		assertThatThrownBy(() -> useTokenUseCase.use(new UseTokenCommand(
				TransactionUuid.from("e4444444-4444-4444-4444-444444444444"),
				MEMBER,
				10L,
				"IMPORT_SCHEDULE",
				"import-1",
				"insufficient"
		))).isInstanceOf(InsufficientTokenBalanceException.class);
	}

	@Test
	void duplicateTransactionUuidIsIdempotent() {
		seedBalances(50L, 0L, 0L);
		UUID tx = UUID.fromString("e5555555-5555-5555-5555-555555555555");
		UseTokenCommand command = new UseTokenCommand(
				new TransactionUuid(tx),
				MEMBER,
				10L,
				"AI_SCHEDULE",
				"schedule-dup",
				"dup use"
		);

		useTokenUseCase.use(command);
		useTokenUseCase.use(command);

		TokenBalanceResult balance = getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER));
		assertThat(balance.paidBalance()).isEqualTo(40L);
		assertThat(getTokenLedgerQueryUseCase.getLedger(new GetTokenLedgerQuery(MEMBER, null, 0, 20)))
				.hasSize(2);
		assertThat(loadTokenLedgerPort.findByTransactionUuid(new TransactionUuid(tx))).isPresent();
	}

	private void seedBalances(long paid, long free, long bonus) {
		if (paid > 0) {
			chargeTokenUseCase.charge(new ChargeTokenCommand(
					TransactionUuid.from("eaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
					MEMBER,
					paid,
					"PAYMENT",
					"seed-paid",
					"seed paid"
			));
		}
		if (free > 0) {
			grantTokenUseCase.grant(GrantTokenCommand.gradeReward(
					TransactionUuid.from("ebbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
					MEMBER,
					free,
					"GOLD",
					"seed free"
			));
		}
		if (bonus > 0) {
			grantTokenUseCase.grant(GrantTokenCommand.bonusReward(
					TransactionUuid.from("eccccccc-cccc-cccc-cccc-cccccccccccc"),
					MEMBER,
					bonus,
					"promo",
					"seed bonus"
			));
		}
	}
}
