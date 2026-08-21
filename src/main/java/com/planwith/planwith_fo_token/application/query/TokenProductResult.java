package com.planwith.planwith_fo_token.application.query;

import com.planwith.planwith_fo_token.domain.model.TokenProductCode;

public record TokenProductResult(
		TokenProductCode code,
		String name,
		long salePrice,
		long baseTokenAmount,
		long bonusTokenAmount,
		long totalTokenAmount
) {
}
