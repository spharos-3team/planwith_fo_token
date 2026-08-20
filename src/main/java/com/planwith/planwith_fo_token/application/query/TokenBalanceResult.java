package com.planwith.planwith_fo_token.application.query;

import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public record TokenBalanceResult(
		MemberUuid memberUuid,
		long paidBalance,
		long freeBalance,
		long bonusBalance,
		long totalBalance
) {
}
