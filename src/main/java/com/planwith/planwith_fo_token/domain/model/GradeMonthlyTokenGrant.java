package com.planwith.planwith_fo_token.domain.model;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

/**
 * 회원·월 단위 등급 무료 토큰 지급 기록. Grade DB를 읽지 않고 이벤트 기준으로만 저장한다.
 */
public final class GradeMonthlyTokenGrant {

	private static final Pattern REWARD_MONTH_PATTERN = Pattern.compile("^\\d{4}-\\d{2}$");

	private final Long grantId;
	private final MemberUuid memberUuid;
	private final String rewardMonth;
	private final UUID eventUuid;
	private final TransactionUuid ledgerTransactionUuid;
	private final long tokenAmount;
	private final String gradeCode;
	private final Instant grantedAt;

	private GradeMonthlyTokenGrant(
			Long grantId,
			MemberUuid memberUuid,
			String rewardMonth,
			UUID eventUuid,
			TransactionUuid ledgerTransactionUuid,
			long tokenAmount,
			String gradeCode,
			Instant grantedAt
	) {
		this.grantId = grantId;
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.rewardMonth = requireRewardMonth(rewardMonth);
		this.eventUuid = Objects.requireNonNull(eventUuid, "Event UUID is required.");
		this.ledgerTransactionUuid = Objects.requireNonNull(ledgerTransactionUuid, "Ledger transaction UUID is required.");
		if (tokenAmount <= 0) {
			throw new IllegalArgumentException("Token amount must be positive.");
		}
		this.tokenAmount = tokenAmount;
		this.gradeCode = gradeCode;
		this.grantedAt = Objects.requireNonNull(grantedAt, "Granted at is required.");
	}

	public static GradeMonthlyTokenGrant recorded(
			MemberUuid memberUuid,
			String rewardMonth,
			UUID eventUuid,
			long tokenAmount,
			String gradeCode,
			Instant grantedAt
	) {
		TransactionUuid ledgerTransactionUuid = ledgerTransactionUuidOf(memberUuid, rewardMonth);
		return new GradeMonthlyTokenGrant(
				null,
				memberUuid,
				rewardMonth,
				eventUuid,
				ledgerTransactionUuid,
				tokenAmount,
				gradeCode,
				grantedAt
		);
	}

	public static GradeMonthlyTokenGrant restore(
			Long grantId,
			MemberUuid memberUuid,
			String rewardMonth,
			UUID eventUuid,
			TransactionUuid ledgerTransactionUuid,
			long tokenAmount,
			String gradeCode,
			Instant grantedAt
	) {
		return new GradeMonthlyTokenGrant(
				grantId,
				memberUuid,
				rewardMonth,
				eventUuid,
				ledgerTransactionUuid,
				tokenAmount,
				gradeCode,
				grantedAt
		);
	}

	/**
	 * 동일 회원·월은 항상 같은 ledger transactionUuid를 사용해 중복 지급을 방지한다.
	 */
	public static TransactionUuid ledgerTransactionUuidOf(MemberUuid memberUuid, String rewardMonth) {
		String key = "GRADE_MONTHLY_FREE:" + memberUuid.value() + ":" + requireRewardMonth(rewardMonth);
		return new TransactionUuid(UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)));
	}

	public static String requireRewardMonth(String rewardMonth) {
		if (rewardMonth == null || rewardMonth.isBlank()) {
			throw new IllegalArgumentException("Reward month is required. format=yyyy-MM");
		}
		String trimmed = rewardMonth.trim();
		if (!REWARD_MONTH_PATTERN.matcher(trimmed).matches()) {
			throw new IllegalArgumentException("Reward month must be in yyyy-MM format. value=" + rewardMonth);
		}
		return trimmed;
	}

	public Long grantId() {
		return grantId;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public String rewardMonth() {
		return rewardMonth;
	}

	public UUID eventUuid() {
		return eventUuid;
	}

	public TransactionUuid ledgerTransactionUuid() {
		return ledgerTransactionUuid;
	}

	public long tokenAmount() {
		return tokenAmount;
	}

	public String gradeCode() {
		return gradeCode;
	}

	public Instant grantedAt() {
		return grantedAt;
	}
}
