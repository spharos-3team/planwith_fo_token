package com.planwith.planwith_fo_token.application.command;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public record HandleGradeRewardGrantedCommand(
		UUID eventUuid,
		MemberUuid memberUuid,
		long tokenAmount,
		String rewardType,
		Instant grantedAt
) {
}
