package com.planwith.planwith_fo_token.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.DeletePaymentMethodCommand;
import com.planwith.planwith_fo_token.application.command.RegisterPaymentMethodCommand;
import com.planwith.planwith_fo_token.application.command.SetDefaultPaymentMethodCommand;
import com.planwith.planwith_fo_token.application.port.in.command.DeletePaymentMethodUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.RegisterPaymentMethodUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.SetDefaultPaymentMethodUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.ListPaymentMethodsQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.application.query.ListPaymentMethodsQuery;
import com.planwith.planwith_fo_token.application.query.PaymentMethodResult;
import com.planwith.planwith_fo_token.domain.exception.PaymentMethodNotFoundException;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.PaymentMethodStatus;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ManagePaymentMethodIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("f4444444-4444-4444-4444-444444444444");

	@Autowired
	private RegisterPaymentMethodUseCase registerPaymentMethodUseCase;

	@Autowired
	private SetDefaultPaymentMethodUseCase setDefaultPaymentMethodUseCase;

	@Autowired
	private DeletePaymentMethodUseCase deletePaymentMethodUseCase;

	@Autowired
	private ListPaymentMethodsQueryUseCase listPaymentMethodsQueryUseCase;

	@Autowired
	private PaymentMethodPort paymentMethodPort;

	@Test
	void setDefaultKeepsSingleDefaultAcrossCards() {
		PaymentMethodResult first = register(MEMBER, "4111111111111111", false);
		PaymentMethodResult second = register(MEMBER, "4222222222222222", false);

		PaymentMethodResult updated = setDefaultPaymentMethodUseCase.setDefault(new SetDefaultPaymentMethodCommand(
				MEMBER,
				new PaymentMethodUuid(second.paymentMethodUuid())
		));

		assertThat(updated.defaultMethod()).isTrue();
		assertThat(paymentMethodPort.findByUuid(first.paymentMethodUuid()).orElseThrow().defaultMethod()).isFalse();
		assertThat(paymentMethodPort.findByUuid(second.paymentMethodUuid()).orElseThrow().defaultMethod()).isTrue();

		List<PaymentMethodResult> listed = listPaymentMethodsQueryUseCase.list(new ListPaymentMethodsQuery(MEMBER));
		assertThat(listed.stream().filter(PaymentMethodResult::defaultMethod)).hasSize(1);
	}

	@Test
	void deleteDefaultPromotesOldestRemainingActiveCard() {
		PaymentMethodResult first = register(MEMBER, "4111111111111111", true);
		PaymentMethodResult second = register(MEMBER, "4222222222222222", false);
		PaymentMethodResult third = register(MEMBER, "4333333333333333", false);

		deletePaymentMethodUseCase.delete(new DeletePaymentMethodCommand(
				MEMBER,
				new PaymentMethodUuid(first.paymentMethodUuid())
		));

		PaymentMethod deleted = paymentMethodPort.findByUuid(first.paymentMethodUuid()).orElseThrow();
		assertThat(deleted.status()).isEqualTo(PaymentMethodStatus.DELETED);
		assertThat(deleted.defaultMethod()).isFalse();

		List<PaymentMethodResult> listed = listPaymentMethodsQueryUseCase.list(new ListPaymentMethodsQuery(MEMBER));
		assertThat(listed).hasSize(2);
		assertThat(listed.stream().filter(PaymentMethodResult::defaultMethod)).hasSize(1);
		assertThat(paymentMethodPort.findByUuid(second.paymentMethodUuid()).orElseThrow().defaultMethod()).isTrue();
		assertThat(paymentMethodPort.findByUuid(third.paymentMethodUuid()).orElseThrow().defaultMethod()).isFalse();
	}

	@Test
	void deleteRejectsAlreadyDeletedCard() {
		PaymentMethodResult first = register(MEMBER, "4111111111111111", true);
		deletePaymentMethodUseCase.delete(new DeletePaymentMethodCommand(
				MEMBER,
				new PaymentMethodUuid(first.paymentMethodUuid())
		));

		assertThatThrownBy(() -> deletePaymentMethodUseCase.delete(new DeletePaymentMethodCommand(
				MEMBER,
				new PaymentMethodUuid(first.paymentMethodUuid())
		))).isInstanceOf(PaymentMethodNotFoundException.class);
	}

	private PaymentMethodResult register(MemberUuid memberUuid, String cardNumber, boolean defaultMethod) {
		return registerPaymentMethodUseCase.register(new RegisterPaymentMethodCommand(
				memberUuid,
				cardNumber,
				"28",
				"12",
				"900101",
				"12",
				defaultMethod
		));
	}
}
