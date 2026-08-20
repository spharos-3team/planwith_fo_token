package com.planwith.planwith_fo_token.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenType;
import com.planwith.planwith_fo_token.domain.model.TransactionType;

public record TokenLedgerEntryResponse(
		Long ledgerId,
		UUID transactionUuid,
		UUID memberUuid,
		TransactionType transactionType,
		TokenType tokenType,
		long amount,
		long balanceAfter,
		ReferenceType referenceType,
		String description,
		Instant occurredAt
) {
}
