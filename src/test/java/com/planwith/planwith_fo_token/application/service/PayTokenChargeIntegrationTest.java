package com.planwith.planwith_fo_token.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.PayTokenChargeCommand;
import com.planwith.planwith_fo_token.application.command.RequestTokenChargeCommand;
import com.planwith.planwith_fo_token.application.port.in.command.PayTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.RequestTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.application.port.out.TokenChargePort;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;
import com.planwith.planwith_fo_token.domain.exception.ChargeAmountMismatchException;
import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.PaymentType;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;
import com.planwith.planwith_fo_token.domain.model.vo.ChargeUuid;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PayTokenChargeIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("b1616161-1616-1616-1616-161616161616");
	private static final PaymentMethodUuid PAYMENT_METHOD =
			PaymentMethodUuid.from("b2626262-2626-2626-2626-262626262626");

	@Autowired
	private RequestTokenChargeUseCase requestTokenChargeUseCase;

	@Autowired
	private PayTokenChargeUseCase payTokenChargeUseCase;

	@Autowired
	private PaymentMethodPort paymentMethodPort;

	@Autowired
	private TokenChargePort tokenChargePort;

	@Autowired
	private GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase;

	@Test
	void billingKeyPaymentCompletesReadyChargeAndGrantsTokens() {
		saveActivePaymentMethod();
		TokenChargeRequestResult ready = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER, "BASIC", PAYMENT_METHOD, PaymentType.BILLING_KEY, "pay-billing-1"
		));

		TokenChargeRequestResult paid = payTokenChargeUseCase.pay(new PayTokenChargeCommand(
				MEMBER,
				new ChargeUuid(ready.chargeUuid()),
				4_900L
		));

		assertThat(paid.status()).isEqualTo(ChargeStatus.PAID);
		TokenCharge saved = tokenChargePort.findByChargeUuid(paid.chargeUuid()).orElseThrow();
		assertThat(saved.status()).isEqualTo(ChargeStatus.PAID);
		assertThat(saved.walletUuid()).isNotNull();
		assertThat(saved.providerPaymentId()).isEqualTo(paid.chargeUuid().toString());
		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).paidBalance())
				.isEqualTo(60L);
	}

	@Test
	void oneTimePaymentUsesSameTokenChargeFlow() {
		TokenChargeRequestResult ready = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER, "TRIAL", null, PaymentType.ONE_TIME, "pay-one-time-1"
		));
		assertThat(ready.paymentType()).isEqualTo(PaymentType.ONE_TIME);
		assertThat(ready.paymentMethodUuid()).isNull();

		TokenChargeRequestResult paid = payTokenChargeUseCase.pay(new PayTokenChargeCommand(
				MEMBER,
				new ChargeUuid(ready.chargeUuid()),
				null
		));

		assertThat(paid.status()).isEqualTo(ChargeStatus.PAID);
		assertThat(paid.tokenAmount()).isEqualTo(10L);
		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).paidBalance())
				.isEqualTo(10L);
	}

	@Test
	void duplicatePayIsIdempotentAndAmountMismatchIsRejected() {
		saveActivePaymentMethod();
		TokenChargeRequestResult ready = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER, "POPULAR", PAYMENT_METHOD, PaymentType.BILLING_KEY, "pay-dup-1"
		));

		assertThatThrownBy(() -> payTokenChargeUseCase.pay(new PayTokenChargeCommand(
				MEMBER, new ChargeUuid(ready.chargeUuid()), 1L
		))).isInstanceOf(ChargeAmountMismatchException.class);

		TokenChargeRequestResult first = payTokenChargeUseCase.pay(new PayTokenChargeCommand(
				MEMBER, new ChargeUuid(ready.chargeUuid()), 9_900L
		));
		TokenChargeRequestResult second = payTokenChargeUseCase.pay(new PayTokenChargeCommand(
				MEMBER, new ChargeUuid(ready.chargeUuid()), 9_900L
		));

		assertThat(first.status()).isEqualTo(ChargeStatus.PAID);
		assertThat(second.chargeUuid()).isEqualTo(first.chargeUuid());
		assertThat(getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).paidBalance())
				.isEqualTo(140L);
	}

	private void saveActivePaymentMethod() {
		paymentMethodPort.save(PaymentMethod.register(
				PAYMENT_METHOD,
				MEMBER,
				"billing-key-pay",
				"신한카드",
				"4242",
				true,
				Instant.parse("2026-08-21T01:00:00Z")
		));
	}
}
