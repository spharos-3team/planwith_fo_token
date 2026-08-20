package com.planwith.planwith_fo_token.adapter.in.web.dto;

import java.util.UUID;

public record TokenBalanceResponse(
		UUID memberUuid,
		long paidBalance,
		long freeBalance,
		long bonusBalance,
		long totalBalance
) {
}
