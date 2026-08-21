package com.planwith.planwith_fo_token.adapter.in.web.dto;

public record TokenProductResponse(
		String code,
		String name,
		long salePrice,
		long baseTokenAmount,
		long bonusTokenAmount,
		long totalTokenAmount
) {
}
