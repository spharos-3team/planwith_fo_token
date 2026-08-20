package com.planwith.planwith_fo_token.application.query;

import com.planwith.planwith_fo_token.domain.model.TransactionType;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public record GetTokenLedgerQuery(
		MemberUuid memberUuid,
		TransactionType transactionType,
		int page,
		int size
) {
}
