package com.planwith.planwith_fo_token.application.query;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentType;
import com.planwith.planwith_fo_token.domain.model.TokenProductCode;

public record TokenChargeRequestResult(
		UUID chargeUuid,
		TokenProductCode productCode,
		ChargeStatus status,
		long tokenAmount,
		long paidAmount,
		UUID paymentMethodUuid,
		PaymentType paymentType,
		Instant createdAt
) {
}
