package com.planwith.planwith_fo_token.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.RegisterPaymentMethodCommand;
import com.planwith.planwith_fo_token.application.port.in.command.RegisterPaymentMethodUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.ListPaymentMethodsQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.application.query.ListPaymentMethodsQuery;
import com.planwith.planwith_fo_token.application.query.PaymentMethodResult;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RegisterPaymentMethodIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("f1111111-1111-1111-1111-111111111111");

	@Autowired
	private RegisterPaymentMethodUseCase registerPaymentMethodUseCase;

	@Autowired
	private ListPaymentMethodsQueryUseCase listPaymentMethodsQueryUseCase;

	@Autowired
	private PaymentMethodPort paymentMethodPort;

	@Test
	void firstCardBecomesDefaultAndPersistsBillingKeyOnlyDisplayFields() {
		PaymentMethodResult first = registerPaymentMethodUseCase.register(registerCommand(false));

		assertThat(first.defaultMethod()).isTrue();
		assertThat(first.cardName()).isEqualTo("Stub Card");
		assertThat(first.fourCardNumber()).isEqualTo("1111");

		PaymentMethod saved = paymentMethodPort.findByUuid(first.paymentMethodUuid()).orElseThrow();
		assertThat(saved.billingKey()).startsWith("stub-billing-key-");
		assertThat(saved.fourCardNumber()).isEqualTo("1111");
	}

	@Test
	void multipleCardsSupportedAndDefaultReassignmentClearsPreviousDefault() {
		PaymentMethodResult first = registerPaymentMethodUseCase.register(registerCommand(false));
		PaymentMethodResult second = registerPaymentMethodUseCase.register(new RegisterPaymentMethodCommand(
				MEMBER,
				"4222222222222222",
				"29",
				"01",
				"900101",
				"34",
				true
		));

		List<PaymentMethodResult> listed = listPaymentMethodsQueryUseCase.list(new ListPaymentMethodsQuery(MEMBER));
		assertThat(listed).hasSize(2);
		assertThat(listed).allMatch(item -> item.paymentMethodUuid() != null);
		assertThat(listed.stream().filter(PaymentMethodResult::defaultMethod)).hasSize(1);

		PaymentMethod previousDefault = paymentMethodPort.findByUuid(first.paymentMethodUuid()).orElseThrow();
		PaymentMethod newDefault = paymentMethodPort.findByUuid(second.paymentMethodUuid()).orElseThrow();
		assertThat(previousDefault.defaultMethod()).isFalse();
		assertThat(newDefault.defaultMethod()).isTrue();
		assertThat(newDefault.fourCardNumber()).isEqualTo("2222");
	}

	@Test
	void listReturnsOnlyActiveCardsForMember() {
		registerPaymentMethodUseCase.register(registerCommand(false));
		PaymentMethodResult second = registerPaymentMethodUseCase.register(new RegisterPaymentMethodCommand(
				MEMBER,
				"4333333333333333",
				"30",
				"06",
				"900101",
				"56",
				false
		));
		paymentMethodPort.save(
				paymentMethodPort.findByUuid(second.paymentMethodUuid()).orElseThrow().delete()
		);

		List<PaymentMethodResult> listed = listPaymentMethodsQueryUseCase.list(new ListPaymentMethodsQuery(MEMBER));
		assertThat(listed).hasSize(1);
		assertThat(listed.get(0).fourCardNumber()).isEqualTo("1111");
	}

	private RegisterPaymentMethodCommand registerCommand(boolean defaultMethod) {
		return new RegisterPaymentMethodCommand(
				MEMBER,
				"4111111111111111",
				"28",
				"12",
				"900101",
				"12",
				defaultMethod
		);
	}
}
