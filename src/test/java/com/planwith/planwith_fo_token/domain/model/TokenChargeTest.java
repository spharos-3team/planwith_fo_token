package com.planwith.planwith_fo_token.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_token.domain.exception.InvalidChargeStateException;
import com.planwith.planwith_fo_token.domain.model.vo.ChargeUuid;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

class TokenChargeTest {

	private static final MemberUuid MEMBER = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private static final Instant NOW = Instant.parse("2026-08-21T00:00:00Z");

	@Test
	void markPaidFailedCanceledOnlyFromReady() {
		TokenCharge ready = TokenCharge.request(
				new ChargeUuid(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
				MEMBER,
				TokenProductCode.TRIAL,
				"client-1",
				null,
				PaymentType.ONE_TIME,
				null,
				10L,
				1_000L,
				NOW
		);

		TokenCharge paid = ready.markPaid(
				new TransactionUuid(ready.chargeUuid().value()),
				"pg-1",
				NOW
		);
		assertThat(paid.status()).isEqualTo(ChargeStatus.PAID);
		assertThat(paid.grantsPaidTokens()).isTrue();

		TokenCharge failed = ready.markFailed("pg-2");
		assertThat(failed.status()).isEqualTo(ChargeStatus.FAILED);
		assertThat(failed.grantsPaidTokens()).isFalse();

		TokenCharge canceled = ready.markCanceled("pg-3");
		assertThat(canceled.status()).isEqualTo(ChargeStatus.CANCELED);

		assertThatThrownBy(() -> paid.markFailed("pg-x"))
				.isInstanceOf(InvalidChargeStateException.class);
		assertThatThrownBy(() -> failed.markCanceled("pg-y"))
				.isInstanceOf(InvalidChargeStateException.class);
	}
}
