package com.planwith.planwith_fo_token.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_token.domain.exception.InvalidPaymentMethodStateException;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

class PaymentMethodPolicyTest {

	private static final MemberUuid MEMBER = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private static final Instant NOW = Instant.parse("2026-08-20T00:00:00Z");

	@Test
	void firstRegisteredMethodShouldBeDefault() {
		assertThat(PaymentMethodPolicy.shouldRegisterAsDefault(List.of(), false)).isTrue();
	}

	@Test
	void additionalMethodIsNotDefaultUnlessRequested() {
		PaymentMethod existing = PaymentMethod.register(
				PaymentMethodUuid.from("22222222-2222-2222-2222-222222222222"),
				MEMBER,
				"billing-1",
				"카드1",
				"1111",
				true,
				NOW
		);

		assertThat(PaymentMethodPolicy.shouldRegisterAsDefault(List.of(existing), false)).isFalse();
		assertThat(PaymentMethodPolicy.shouldRegisterAsDefault(List.of(existing), true)).isTrue();
	}

	@Test
	void applyDefaultChangeKeepsSingleDefault() {
		PaymentMethodUuid firstUuid = PaymentMethodUuid.from("22222222-2222-2222-2222-222222222222");
		PaymentMethodUuid secondUuid = PaymentMethodUuid.from("33333333-3333-3333-3333-333333333333");
		PaymentMethod first = PaymentMethod.register(firstUuid, MEMBER, "billing-1", "카드1", "1111", true, NOW);
		PaymentMethod second = PaymentMethod.register(secondUuid, MEMBER, "billing-2", "카드2", "2222", false, NOW);

		PaymentMethodPolicy.DefaultChangeResult result = PaymentMethodPolicy.applyDefaultChange(
				List.of(first, second),
				secondUuid
		);

		assertThat(result.updatedMethods()).hasSize(2);
		assertThat(find(result.updatedMethods(), firstUuid).defaultMethod()).isFalse();
		assertThat(find(result.updatedMethods(), secondUuid).defaultMethod()).isTrue();
	}

	@Test
	void applyDefaultChangeFailsForInactiveMethod() {
		PaymentMethodUuid uuid = PaymentMethodUuid.from("22222222-2222-2222-2222-222222222222");
		PaymentMethod deleted = PaymentMethod.register(uuid, MEMBER, "billing-1", "카드1", "1111", false, NOW).delete();

		assertThatThrownBy(() -> PaymentMethodPolicy.applyDefaultChange(List.of(deleted), uuid))
				.isInstanceOf(InvalidPaymentMethodStateException.class);
	}

	@Test
	void applyOnDeletePromotesOldestRemainingWhenDefaultDeleted() {
		PaymentMethodUuid firstUuid = PaymentMethodUuid.from("22222222-2222-2222-2222-222222222222");
		PaymentMethodUuid secondUuid = PaymentMethodUuid.from("33333333-3333-3333-3333-333333333333");
		PaymentMethod first = PaymentMethod.register(firstUuid, MEMBER, "billing-1", "카드1", "1111", true, NOW);
		PaymentMethod second = PaymentMethod.register(
				secondUuid, MEMBER, "billing-2", "카드2", "2222", false, NOW.plusSeconds(60)
		);

		PaymentMethodPolicy.DeleteResult result = PaymentMethodPolicy.applyOnDelete(first, List.of(first, second));

		assertThat(result.deleted().status().name()).isEqualTo("DELETED");
		assertThat(result.deleted().defaultMethod()).isFalse();
		assertThat(result.promotedDefault().paymentMethodUuid()).isEqualTo(secondUuid);
		assertThat(result.promotedDefault().defaultMethod()).isTrue();
	}

	@Test
	void applyOnDeleteLeavesNoDefaultWhenLastActiveDeleted() {
		PaymentMethodUuid uuid = PaymentMethodUuid.from("22222222-2222-2222-2222-222222222222");
		PaymentMethod only = PaymentMethod.register(uuid, MEMBER, "billing-1", "카드1", "1111", true, NOW);

		PaymentMethodPolicy.DeleteResult result = PaymentMethodPolicy.applyOnDelete(only, List.of(only));

		assertThat(result.deleted().status().name()).isEqualTo("DELETED");
		assertThat(result.promotedDefault()).isNull();
		assertThat(result.remainingActive()).isEmpty();
	}

	private PaymentMethod find(List<PaymentMethod> methods, PaymentMethodUuid uuid) {
		return methods.stream()
				.filter(method -> method.paymentMethodUuid().equals(uuid))
				.findFirst()
				.orElseThrow();
	}
}
