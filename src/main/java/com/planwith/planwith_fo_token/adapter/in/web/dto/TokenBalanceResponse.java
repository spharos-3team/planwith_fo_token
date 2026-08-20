package com.planwith.planwith_fo_token.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
		"totalBalance",
		"paidBalance",
		"freeBalance",
		"bonusBalance",
		"memberUuid"
})
public record TokenBalanceResponse(
		long totalBalance,
		long paidBalance,
		long freeBalance,
		long bonusBalance,
		java.util.UUID memberUuid
) {
}
