package com.planwith.planwith_fo_token.adapter.out.persistence.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.application.port.out.TokenChargePort;
import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.PaymentMethodStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentType;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;
import com.planwith.planwith_fo_token.domain.model.vo.ChargeUuid;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentAndChargePersistenceIntegrationTest {

	@Autowired
	private PaymentMethodPort paymentMethodPort;

	@Autowired
	private TokenChargePort tokenChargePort;

	@Test
	void savesPaymentMethodAndChargeLinkedToLedgerUuid() {
		MemberUuid memberUuid = MemberUuid.from("33333333-3333-3333-3333-333333333333");
		PaymentMethodUuid paymentMethodUuid = PaymentMethodUuid.from("44444444-4444-4444-4444-444444444444");
		ChargeUuid chargeUuid = ChargeUuid.from("55555555-5555-5555-5555-555555555555");
		TransactionUuid ledgerUuid = TransactionUuid.from("66666666-6666-6666-6666-666666666666");
		Instant now = Instant.parse("2026-08-20T02:00:00Z");

		PaymentMethod savedMethod = paymentMethodPort.save(PaymentMethod.restore(
				null,
				paymentMethodUuid,
				memberUuid,
				"billing-key-masked",
				"신한카드",
				"1234",
				true,
				PaymentMethodStatus.ACTIVE,
				now
		));

		TokenCharge savedCharge = tokenChargePort.save(TokenCharge.restore(
				null,
				chargeUuid,
				memberUuid,
				null,
				null,
				ledgerUuid,
				savedMethod.paymentMethodUuid(),
				PaymentType.BILLING_KEY,
				"provider-pay-1",
				100L,
				"billing-key-masked",
				4900L,
				ChargeStatus.PAID,
				now,
				now
		));

		assertThat(savedMethod.paymentMethodId()).isNotNull();
		assertThat(savedCharge.grantsPaidTokens()).isTrue();
		assertThat(savedCharge.walletUuid()).isEqualTo(ledgerUuid);
		assertThat(tokenChargePort.findByChargeUuid(chargeUuid.value())).isPresent();
	}
}
