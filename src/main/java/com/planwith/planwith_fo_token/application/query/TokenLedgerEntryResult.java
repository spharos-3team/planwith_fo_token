package com.planwith.planwith_fo_token.application.query;

import java.time.Instant;

import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenType;
import com.planwith.planwith_fo_token.domain.model.TransactionType;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

public record TokenLedgerEntryResult(
		Long ledgerId,
		TransactionUuid transactionUuid,
		MemberUuid memberUuid,
		Instant occurredAt,
		TransactionType transactionType,
		TokenType tokenType,
		long amount,
		long amountChange,
		long balanceAfter,
		ReferenceType usagePlace,
		String description
) {
}
