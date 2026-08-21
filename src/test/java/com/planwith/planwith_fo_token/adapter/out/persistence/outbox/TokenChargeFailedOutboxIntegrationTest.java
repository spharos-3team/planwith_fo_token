package com.planwith.planwith_fo_token.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.adapter.out.pg.portone.PortOnePaymentAdapter;
import com.planwith.planwith_fo_token.application.command.ConfirmTokenChargeCommand;
import com.planwith.planwith_fo_token.application.command.RequestTokenChargeCommand;
import com.planwith.planwith_fo_token.application.event.TokenChargeFailedEvent;
import com.planwith.planwith_fo_token.application.port.in.command.ConfirmTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.RequestTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;
import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentType;
import com.planwith.planwith_fo_token.domain.model.vo.ChargeUuid;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TokenChargeFailedOutboxIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("a2020202-2020-2020-2020-202020202020");

	@Autowired
	private RequestTokenChargeUseCase requestTokenChargeUseCase;

	@Autowired
	private ConfirmTokenChargeUseCase confirmTokenChargeUseCase;

	@Autowired
	private PortOnePaymentAdapter portOnePaymentAdapter;

	@Autowired
	private SpringDataTokenOutboxRepository outboxRepository;

	@Test
	void failedPaymentVerificationPublishesTokenChargeFailedOutbox() {
		TokenChargeRequestResult ready = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER, "TRIAL", null, PaymentType.ONE_TIME, "kafka-fail-1"
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
		TokenOutboxJpaEntity outbox = outboxRepository.findByEventUuid(ready.chargeUuid()).orElseThrow();
		assertThat(outbox.eventType()).isEqualTo(TokenChargeFailedEvent.EVENT_TYPE);
		assertThat(outbox.aggregateType()).isEqualTo(TokenChargeFailedEvent.AGGREGATE_TYPE);
		assertThat(outbox.publishedAt()).isNull();
		assertThat(outbox.payload()).contains("PG_PAYMENT_FAILED");
	}
}
