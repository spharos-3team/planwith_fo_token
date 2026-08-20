package com.planwith.planwith_fo_token.application.service;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.GetTokenLedgerQuery;
import com.planwith.planwith_fo_token.application.query.TokenBalanceResult;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TokenCqrsFlowIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("33333333-3333-3333-3333-333333333333");

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

	@Test
	void commandAndQueryFlowThroughPersistenceAdapters() {
		UUID chargeTx = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
		UUID grantTx = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
		UUID useTx = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

		chargeTokenUseCase.charge(new ChargeTokenCommand(
				new TransactionUuid(chargeTx),
				MEMBER,
				100L,
				"PAYMENT",
				"payment-1",
				"charge"
		));
		grantTokenUseCase.grant(GrantTokenCommand.gradeReward(
				new TransactionUuid(grantTx),
				MEMBER,
				20L,
				"grade-1",
				"grade reward"
		));
		useTokenUseCase.use(new UseTokenCommand(
				new TransactionUuid(useTx),
				MEMBER,
				30L,
				"AI_SCHEDULE",
				"schedule-1",
				"use"
		));

		TokenBalanceResult balance = getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER));
		assertThat(balance.totalBalance()).isEqualTo(90L);
		assertThat(balance.paidBalance()).isEqualTo(90L);
		assertThat(balance.freeBalance()).isZero();

		assertThat(getTokenLedgerQueryUseCase.getLedger(new GetTokenLedgerQuery(MEMBER, null, 0, 10)))
				.hasSize(3);

		chargeTokenUseCase.charge(new ChargeTokenCommand(
				new TransactionUuid(chargeTx),
				MEMBER,
				100L,
				"PAYMENT",
				"payment-1",
				"charge"
		));
		assertThat(getTokenLedgerQueryUseCase.getLedger(new GetTokenLedgerQuery(MEMBER, null, 0, 10)))
				.hasSize(3);
	}
}
