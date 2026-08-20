package com.planwith.planwith_fo_token.adapter.out.persistence.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.PaymentMethodStatus;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentMethodPersistenceAdapterIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private static final Instant NOW = Instant.parse("2026-08-20T00:00:00Z");

	@Autowired
	private PaymentMethodPort paymentMethodPort;

	@Test
	void findsActiveDefaultAndMemberScopedPaymentMethods() {
		PaymentMethodUuid defaultUuid = PaymentMethodUuid.from("e1111111-1111-1111-1111-111111111111");
		PaymentMethodUuid secondaryUuid = PaymentMethodUuid.from("e2222222-2222-2222-2222-222222222222");

		paymentMethodPort.save(PaymentMethod.register(
				defaultUuid, MEMBER, "billing-default", "기본카드", "1111", true, NOW
		));
		paymentMethodPort.save(PaymentMethod.register(
				secondaryUuid, MEMBER, "billing-secondary", "보조카드", "2222", false, NOW
		));
		paymentMethodPort.save(PaymentMethod.register(
				PaymentMethodUuid.from("e3333333-3333-3333-3333-333333333333"),
				MEMBER,
				"billing-deleted",
				"삭제카드",
				"3333",
				false,
				NOW
		).delete());

		List<PaymentMethod> activeMethods = paymentMethodPort.findActiveByMemberUuid(MEMBER);
		PaymentMethod defaultMethod = paymentMethodPort.findDefaultActiveByMemberUuid(MEMBER).orElseThrow();
		PaymentMethod memberScoped = paymentMethodPort.findByUuidAndMemberUuid(defaultUuid, MEMBER).orElseThrow();

		assertThat(activeMethods).hasSize(2);
		assertThat(defaultMethod.paymentMethodUuid()).isEqualTo(defaultUuid);
		assertThat(defaultMethod.defaultMethod()).isTrue();
		assertThat(memberScoped.cardName()).isEqualTo("기본카드");
		assertThat(activeMethods).allMatch(method -> method.status() == PaymentMethodStatus.ACTIVE);
	}
}
