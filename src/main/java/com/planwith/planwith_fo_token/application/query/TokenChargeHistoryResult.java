package com.planwith.planwith_fo_token.application.query;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentType;

public record TokenChargeHistoryResult(
		UUID chargeUuid,
		String paymentCode,
		Instant chargedAt,
		long tokenAmount,
		long paidAmount,
		PaymentType paymentType,
		String paymentMethodName,
		String cardLastFour,
		ChargeStatus status
) {
}
