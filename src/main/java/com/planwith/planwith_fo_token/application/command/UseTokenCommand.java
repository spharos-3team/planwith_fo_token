package com.planwith.planwith_fo_token.application.command;

import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

public record UseTokenCommand(
		TransactionUuid transactionUuid,
		MemberUuid memberUuid,
		long amount,
		String referenceType,
		String referenceUuid
) {
}
