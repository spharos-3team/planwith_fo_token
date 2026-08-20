package com.planwith.planwith_fo_token.adapter.in.kafka.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentCompletedInboundEvent(
		String eventUuid,
		String memberUuid,
		Long tokenAmount,
		String paymentReference,
		Instant completedAt
) {
}
