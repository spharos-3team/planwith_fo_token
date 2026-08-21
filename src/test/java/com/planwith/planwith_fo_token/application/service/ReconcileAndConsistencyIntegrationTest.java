package com.planwith.planwith_fo_token.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.adapter.out.pg.portone.PortOnePaymentAdapter;
import com.planwith.planwith_fo_token.application.command.ReconcileTokenChargeCommand;
import com.planwith.planwith_fo_token.application.command.RequestTokenChargeCommand;
import com.planwith.planwith_fo_token.application.port.in.command.ReconcileTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.RequestTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.VerifyWalletLedgerConsistencyQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.TokenChargePort;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;
import com.planwith.planwith_fo_token.application.query.VerifyWalletLedgerConsistencyQuery;
import com.planwith.planwith_fo_token.application.query.WalletLedgerConsistencyResult;
import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentType;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;
import com.planwith.planwith_fo_token.domain.model.vo.ChargeUuid;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReconcileAndConsistencyIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("c2121212-2121-2121-2121-212121212121");

	@Autowired
	private RequestTokenChargeUseCase requestTokenChargeUseCase;

	@Autowired
	private ReconcileTokenChargeUseCase reconcileTokenChargeUseCase;

	@Autowired
	private PortOnePaymentAdapter portOnePaymentAdapter;

	@Autowired
	private TokenChargePort tokenChargePort;

	@Autowired
	private GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase;

	@Autowired
	private VerifyWalletLedgerConsistencyQueryUseCase verifyWalletLedgerConsistencyQueryUseCase;

	@Test
	void reconcileRecoversReadyChargeWhenPgAlreadyPaid() {
		TokenChargeRequestResult ready = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER, "BASIC", null, PaymentType.ONE_TIME, "reconcile-ready-1"
		));
		portOnePaymentAdapter.putStubPayment(ready.chargeUuid().toString(), "PAID", 4_900L);

		TokenChargeRequestResult recovered = reconcileTokenChargeUseCase.reconcile(new ReconcileTokenChargeCommand(
				MEMBER,
				new ChargeUuid(ready.chargeUuid())
		));

		assertThat(recovered.status()).isEqualTo(ChargeStatus.PAID);
		TokenCharge saved = tokenChargePort.findByChargeUuid(ready.chargeUuid()).orElseThrow();
		assertThat(saved.status()).isEqualTo(ChargeStatus.PAID);
		assertThat(saved.walletUuid()).isNotNull();
		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).paidBalance())
				.isEqualTo(60L);

		TokenChargeRequestResult duplicate = reconcileTokenChargeUseCase.reconcile(new ReconcileTokenChargeCommand(
				MEMBER,
				new ChargeUuid(ready.chargeUuid())
		));
		assertThat(duplicate.status()).isEqualTo(ChargeStatus.PAID);
		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).paidBalance())
				.isEqualTo(60L);
	}

	@Test
	void verifyWalletLedgerConsistencyPassesAfterRecovery() {
		TokenChargeRequestResult ready = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER, "TRIAL", null, PaymentType.ONE_TIME, "reconcile-consistency-1"
		));
		portOnePaymentAdapter.putStubPayment(ready.chargeUuid().toString(), "PAID", 1_000L);
		reconcileTokenChargeUseCase.reconcile(new ReconcileTokenChargeCommand(
				MEMBER,
				new ChargeUuid(ready.chargeUuid())
		));

		WalletLedgerConsistencyResult result = verifyWalletLedgerConsistencyQueryUseCase.verify(
				new VerifyWalletLedgerConsistencyQuery(MEMBER)
		);
		assertThat(result.consistent()).isTrue();
		assertThat(result.walletTotalBalance()).isEqualTo(10L);
		assertThat(result.ledgerBalanceAfter()).isEqualTo(10L);
	}
}
