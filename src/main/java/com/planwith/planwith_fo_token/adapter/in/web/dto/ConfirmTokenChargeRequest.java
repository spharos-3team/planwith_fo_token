package com.planwith.planwith_fo_token.adapter.in.web.dto;

public record ConfirmTokenChargeRequest(
		String providerPaymentId,
		Long paidAmount
) {
}
