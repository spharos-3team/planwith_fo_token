package com.planwith.planwith_fo_token.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenType;
import com.planwith.planwith_fo_token.domain.model.TransactionType;

@JsonPropertyOrder({
		"occurredAt",
		"transactionType",
		"amountChange",
		"amount",
		"balanceAfter",
		"usagePlace",
		"description",
		"tokenType",
		"transactionUuid",
		"ledgerId"
})
public record TokenLedgerEntryResponse(
		Instant occurredAt,
		TransactionType transactionType,
		long amountChange,
		long amount,
		long balanceAfter,
		ReferenceType usagePlace,
		String description,
		TokenType tokenType,
		UUID transactionUuid,
		Long ledgerId
) {
}
