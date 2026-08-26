package com.planwith.planwith_fo_token.adapter.in.kafka.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GradeInitialBonusGrantedInboundEvent(
		String eventUuid,
		String memberUuid,
		Long tokenAmount,
		String gradeCode,
		Instant grantedAt
) {
}
