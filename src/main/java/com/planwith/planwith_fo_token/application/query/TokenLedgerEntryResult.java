package com.planwith.planwith_fo_token.application.query;

import java.time.Instant;

import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntryType;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

public record TokenLedgerEntryResult(
		Long ledgerId,
		TransactionUuid transactionUuid,
		MemberUuid memberUuid,
		TokenLedgerEntryType entryType,
		long amount,
		long balanceAfter,
		String referenceType,
		String referenceUuid,
		Instant occurredAt
) {
}
