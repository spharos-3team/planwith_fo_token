package com.planwith.planwith_fo_token.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

class GradeMonthlyTokenGrantTest {

	private static final MemberUuid MEMBER = MemberUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

	@Test
	void sameMemberAndMonthShareLedgerTransactionUuid() {
		TransactionUuid first = GradeMonthlyTokenGrant.ledgerTransactionUuidOf(MEMBER, "2026-08");
		TransactionUuid second = GradeMonthlyTokenGrant.ledgerTransactionUuidOf(MEMBER, "2026-08");
		TransactionUuid otherMonth = GradeMonthlyTokenGrant.ledgerTransactionUuidOf(MEMBER, "2026-09");
		TransactionUuid expire = GradeMonthlyTokenGrant.expireLedgerTransactionUuidOf(MEMBER, "2026-08");

		assertThat(first).isEqualTo(second);
		assertThat(first).isNotEqualTo(otherMonth);
		assertThat(expire).isNotEqualTo(first);
	}

	@Test
	void recordedRequiresValidRewardMonth() {
		UUID eventUuid = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
		GradeMonthlyTokenGrant grant = GradeMonthlyTokenGrant.recorded(
				MEMBER,
				"2026-08",
				eventUuid,
				30L,
				"GOLD",
				Instant.parse("2026-08-01T00:00:00Z")
		);

		assertThat(grant.rewardMonth()).isEqualTo("2026-08");
		assertThat(grant.ledgerTransactionUuid())
				.isEqualTo(GradeMonthlyTokenGrant.ledgerTransactionUuidOf(MEMBER, "2026-08"));

		assertThatThrownBy(() -> GradeMonthlyTokenGrant.requireRewardMonth("2026/08"))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
