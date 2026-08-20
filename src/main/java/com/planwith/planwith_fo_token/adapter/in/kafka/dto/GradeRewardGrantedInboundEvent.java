package com.planwith.planwith_fo_token.adapter.in.kafka.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GradeRewardGrantedInboundEvent(
		String eventUuid,
		String memberUuid,
		String gradeCode,
		Integer gradeLevel,
		String rewardMonth,
		Long tokenAmount,
		String rewardType,
		Instant grantedAt
) {
}
