package com.planwith.planwith_fo_token.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.RequestTokenChargeCommand;
import com.planwith.planwith_fo_token.application.port.in.command.RequestTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.ListTokenProductsQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.application.port.out.TokenChargePort;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;
import com.planwith.planwith_fo_token.domain.exception.PaymentMethodNotFoundException;
import com.planwith.planwith_fo_token.domain.exception.TokenProductNotFoundException;
import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.PaymentType;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;
import com.planwith.planwith_fo_token.domain.model.TokenProductCode;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RequestTokenChargeIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("a5151515-1515-1515-1515-151515151515");
	private static final PaymentMethodUuid PAYMENT_METHOD =
			PaymentMethodUuid.from("a6161616-1616-1616-1616-161616161616");

	@Autowired
	private RequestTokenChargeUseCase requestTokenChargeUseCase;

	@Autowired
	private ListTokenProductsQueryUseCase listTokenProductsQueryUseCase;

	@Autowired
	private PaymentMethodPort paymentMethodPort;

	@Autowired
	private TokenChargePort tokenChargePort;

	@Test
	void listsServerProducts() {
		assertThat(listTokenProductsQueryUseCase.listProducts()).hasSize(4);
	}

	@Test
	void createsReadyChargeWithServerControlledAmounts() {
		saveActivePaymentMethod();

		TokenChargeRequestResult result = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER,
				"BASIC",
				PAYMENT_METHOD,
				PaymentType.BILLING_KEY,
				"idem-basic-1"
		));

		assertThat(result.status()).isEqualTo(ChargeStatus.READY);
		assertThat(result.productCode()).isEqualTo(TokenProductCode.BASIC);
		assertThat(result.paidAmount()).isEqualTo(4_900L);
		assertThat(result.tokenAmount()).isEqualTo(60L);

		TokenCharge saved = tokenChargePort.findByChargeUuid(result.chargeUuid()).orElseThrow();
		assertThat(saved.status()).isEqualTo(ChargeStatus.READY);
		assertThat(saved.billingKey()).isEqualTo("billing-key-ready");
		assertThat(saved.memberUuid()).isEqualTo(MEMBER);
	}

	@Test
	void duplicateClientRequestIdReturnsSameCharge() {
		saveActivePaymentMethod();

		TokenChargeRequestResult first = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER, "POPULAR", PAYMENT_METHOD, null, "idem-dup-1"
		));
		TokenChargeRequestResult second = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER, "POPULAR", PAYMENT_METHOD, null, "idem-dup-1"
		));

		assertThat(second.chargeUuid()).isEqualTo(first.chargeUuid());
		assertThat(second.tokenAmount()).isEqualTo(140L);
		assertThat(second.paidAmount()).isEqualTo(9_900L);
	}

	@Test
	void rejectsUnknownProductAndMissingPaymentMethod() {
		assertThatThrownBy(() -> requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER, "UNKNOWN", PAYMENT_METHOD, null, null
		))).isInstanceOf(TokenProductNotFoundException.class);

		assertThatThrownBy(() -> requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER, "TRIAL", PAYMENT_METHOD, null, null
		))).isInstanceOf(PaymentMethodNotFoundException.class);
	}

	private void saveActivePaymentMethod() {
		paymentMethodPort.save(PaymentMethod.register(
				PAYMENT_METHOD,
				MEMBER,
				"billing-key-ready",
				"신한카드",
				"1234",
				true,
				Instant.parse("2026-08-21T00:00:00Z")
		));
	}
}
