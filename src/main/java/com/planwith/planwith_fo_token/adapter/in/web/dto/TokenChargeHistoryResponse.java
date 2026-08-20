package com.planwith.planwith_fo_token.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentType;

@JsonPropertyOrder({
		"paymentCode",
		"chargedAt",
		"tokenAmount",
		"paidAmount",
		"paymentMethodName",
		"cardLastFour",
		"status",
		"paymentType",
		"chargeUuid"
})
public record TokenChargeHistoryResponse(
		String paymentCode,
		Instant chargedAt,
		long tokenAmount,
		long paidAmount,
		String paymentMethodName,
		String cardLastFour,
		ChargeStatus status,
		PaymentType paymentType,
		UUID chargeUuid
) {
}
