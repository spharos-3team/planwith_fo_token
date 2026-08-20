package com.planwith.planwith_fo_token.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_token.domain.exception.InvalidPaymentMethodStateException;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

class PaymentMethodTest {

	private static final MemberUuid MEMBER = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private static final PaymentMethodUuid METHOD_UUID = PaymentMethodUuid.from("22222222-2222-2222-2222-222222222222");
	private static final Instant NOW = Instant.parse("2026-08-20T00:00:00Z");

	@Test
	void registerCreatesActivePaymentMethod() {
		PaymentMethod method = PaymentMethod.register(
				METHOD_UUID,
				MEMBER,
				"billing-key",
				"신한카드",
				"1234",
				true,
				NOW
		);

		assertThat(method.status()).isEqualTo(PaymentMethodStatus.ACTIVE);
		assertThat(method.defaultMethod()).isTrue();
		assertThat(method.billingKey()).isEqualTo("billing-key");
	}

	@Test
	void deletedMethodCannotBeDefault() {
		assertThatThrownBy(() -> PaymentMethod.restore(
				1L,
				METHOD_UUID,
				MEMBER,
				"billing-key",
				"신한카드",
				"1234",
				true,
				PaymentMethodStatus.DELETED,
				NOW
		)).isInstanceOf(InvalidPaymentMethodStateException.class);
	}

	@Test
	void deleteClearsDefaultAndChangesStatus() {
		PaymentMethod method = PaymentMethod.register(
				METHOD_UUID, MEMBER, "billing-key", "신한카드", "1234", true, NOW
		);

		PaymentMethod deleted = method.delete();

		assertThat(deleted.status()).isEqualTo(PaymentMethodStatus.DELETED);
		assertThat(deleted.defaultMethod()).isFalse();
	}

	@Test
	void expireClearsDefaultAndChangesStatus() {
		PaymentMethod method = PaymentMethod.register(
				METHOD_UUID, MEMBER, "billing-key", "신한카드", "1234", true, NOW
		);

		PaymentMethod expired = method.expire();

		assertThat(expired.status()).isEqualTo(PaymentMethodStatus.EXPIRED);
		assertThat(expired.defaultMethod()).isFalse();
	}

	@Test
	void inactiveMethodCannotBeMarkedDefault() {
		PaymentMethod deleted = PaymentMethod.register(
				METHOD_UUID, MEMBER, "billing-key", "신한카드", "1234", false, NOW
		).delete();

		assertThatThrownBy(deleted::markAsDefault)
				.isInstanceOf(InvalidPaymentMethodStateException.class);
	}
}
