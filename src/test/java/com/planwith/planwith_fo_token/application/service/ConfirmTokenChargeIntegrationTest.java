package com.planwith.planwith_fo_token.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.adapter.out.pg.portone.PortOnePaymentAdapter;
import com.planwith.planwith_fo_token.application.command.ConfirmTokenChargeCommand;
import com.planwith.planwith_fo_token.application.command.RequestTokenChargeCommand;
import com.planwith.planwith_fo_token.application.port.in.command.ConfirmTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.RequestTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.TokenChargePort;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;
import com.planwith.planwith_fo_token.domain.exception.ChargeAmountMismatchException;
import com.planwith.planwith_fo_token.domain.exception.InvalidChargeStateException;
import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentType;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;
import com.planwith.planwith_fo_token.domain.model.vo.ChargeUuid;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ConfirmTokenChargeIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("c1717171-1717-1717-1717-171717171717");

	@Autowired
	private RequestTokenChargeUseCase requestTokenChargeUseCase;

	@Autowired
	private ConfirmTokenChargeUseCase confirmTokenChargeUseCase;

	@Autowired
	private TokenChargePort tokenChargePort;

	@Autowired
	private GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase;

	@Autowired
	private PortOnePaymentAdapter portOnePaymentAdapter;

	@Test
	void confirmAfterPgPaidGrantsPaidTokensOnce() {
		TokenChargeRequestResult ready = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER, "BASIC", null, PaymentType.ONE_TIME, "confirm-paid-1"
		));
		String providerPaymentId = ready.chargeUuid().toString();
		portOnePaymentAdapter.putStubPayment(providerPaymentId, "PAID", 4_900L);

		TokenChargeRequestResult paid = confirmTokenChargeUseCase.confirm(new ConfirmTokenChargeCommand(
				MEMBER,
				new ChargeUuid(ready.chargeUuid()),
				providerPaymentId,
				4_900L
		));
		TokenChargeRequestResult duplicate = confirmTokenChargeUseCase.confirm(new ConfirmTokenChargeCommand(
				MEMBER,
				new ChargeUuid(ready.chargeUuid()),
				providerPaymentId,
				4_900L
		));

		assertThat(paid.status()).isEqualTo(ChargeStatus.PAID);
		assertThat(duplicate.chargeUuid()).isEqualTo(paid.chargeUuid());
		TokenCharge saved = tokenChargePort.findByChargeUuid(paid.chargeUuid()).orElseThrow();
		assertThat(saved.walletUuid()).isNotNull();
		assertThat(saved.providerPaymentId()).isEqualTo(providerPaymentId);
		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).paidBalance())
				.isEqualTo(60L);
	}

	@Test
	void confirmMarksFailedWhenPgAmountMismatches() {
		TokenChargeRequestResult ready = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER, "TRIAL", null, PaymentType.ONE_TIME, "confirm-amount-1"
		));
		String providerPaymentId = ready.chargeUuid().toString();
		portOnePaymentAdapter.putStubPayment(providerPaymentId, "PAID", 999L);

		TokenChargeRequestResult failed = confirmTokenChargeUseCase.confirm(new ConfirmTokenChargeCommand(
				MEMBER,
				new ChargeUuid(ready.chargeUuid()),
				providerPaymentId,
				null
		));

		assertThat(failed.status()).isEqualTo(ChargeStatus.FAILED);
		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).paidBalance())
				.isZero();
		assertThatThrownBy(() -> confirmTokenChargeUseCase.confirm(new ConfirmTokenChargeCommand(
				MEMBER,
				new ChargeUuid(ready.chargeUuid()),
				providerPaymentId,
				null
		))).isInstanceOf(InvalidChargeStateException.class);
	}

	@Test
	void confirmMarksCanceledWhenPgCanceled() {
		TokenChargeRequestResult ready = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER, "TRIAL", null, PaymentType.ONE_TIME, "confirm-cancel-1"
		));
		String providerPaymentId = ready.chargeUuid().toString();
		portOnePaymentAdapter.putStubPayment(providerPaymentId, "CANCELLED", 1_000L);

		TokenChargeRequestResult canceled = confirmTokenChargeUseCase.confirm(new ConfirmTokenChargeCommand(
				MEMBER,
				new ChargeUuid(ready.chargeUuid()),
				providerPaymentId,
				null
		));

		assertThat(canceled.status()).isEqualTo(ChargeStatus.CANCELED);
		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).paidBalance())
				.isZero();
	}

	@Test
	void confirmMarksFailedWhenPgFailed() {
		TokenChargeRequestResult ready = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER, "TRIAL", null, PaymentType.ONE_TIME, "confirm-fail-1"
		));
		String providerPaymentId = ready.chargeUuid().toString();
		portOnePaymentAdapter.putStubPayment(providerPaymentId, "FAILED", 1_000L);

		TokenChargeRequestResult failed = confirmTokenChargeUseCase.confirm(new ConfirmTokenChargeCommand(
				MEMBER,
				new ChargeUuid(ready.chargeUuid()),
				providerPaymentId,
				null
		));

		assertThat(failed.status()).isEqualTo(ChargeStatus.FAILED);
		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).paidBalance())
				.isZero();
	}

	@Test
	void confirmRejectsClientAmountMismatchBeforePgLookup() {
		TokenChargeRequestResult ready = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER, "TRIAL", null, PaymentType.ONE_TIME, "confirm-client-amount-1"
		));

		assertThatThrownBy(() -> confirmTokenChargeUseCase.confirm(new ConfirmTokenChargeCommand(
				MEMBER,
				new ChargeUuid(ready.chargeUuid()),
				ready.chargeUuid().toString(),
				1L
		))).isInstanceOf(ChargeAmountMismatchException.class);

		TokenCharge saved = tokenChargePort.findByChargeUuid(ready.chargeUuid()).orElseThrow();
		assertThat(saved.status()).isEqualTo(ChargeStatus.READY);
	}
}
