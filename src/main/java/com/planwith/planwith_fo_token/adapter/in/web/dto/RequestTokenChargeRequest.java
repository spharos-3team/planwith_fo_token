package com.planwith.planwith_fo_token.adapter.in.web.dto;

import java.util.UUID;

public record RequestTokenChargeRequest(
		String productCode,
		UUID paymentMethodUuid,
		String paymentType,
		String clientRequestId
) {
}
