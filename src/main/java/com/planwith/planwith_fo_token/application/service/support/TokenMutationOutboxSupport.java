package com.planwith.planwith_fo_token.application.service.support;

import com.planwith.planwith_fo_token.application.event.TokenChargedEvent;
import com.planwith.planwith_fo_token.application.event.TokenExpiredEvent;
import com.planwith.planwith_fo_token.application.event.TokenRewardedEvent;
import com.planwith.planwith_fo_token.application.event.TokenUsedEvent;
import com.planwith.planwith_fo_token.application.port.out.TokenOutboxMessage;
import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TransactionType;

final class TokenMutationOutboxSupport {

	private TokenMutationOutboxSupport() {
	}

	static TokenOutboxMessage toOutboxMessage(TokenLedger ledger) {
		return new TokenOutboxMessage(
				ledger.transactionUuid().toString(),
				resolveAggregateType(ledger),
				ledger.memberUuid().toString(),
				resolveEventType(ledger),
				buildPayload(ledger),
				ledger.occurredAt()
		);
	}

	private static String resolveEventType(TokenLedger ledger) {
		return switch (ledger.transactionType()) {
			case CHARGE -> TokenChargedEvent.EVENT_TYPE;
			case REWARD -> TokenRewardedEvent.EVENT_TYPE;
			case USE -> TokenUsedEvent.EVENT_TYPE;
			case EXPIRE -> TokenExpiredEvent.EVENT_TYPE;
		};
	}

	private static String resolveAggregateType(TokenLedger ledger) {
		return switch (ledger.transactionType()) {
			case CHARGE -> TokenChargedEvent.AGGREGATE_TYPE;
			case REWARD -> TokenRewardedEvent.AGGREGATE_TYPE;
			case USE -> TokenUsedEvent.AGGREGATE_TYPE;
			case EXPIRE -> TokenExpiredEvent.AGGREGATE_TYPE;
		};
	}

	private static String buildPayload(TokenLedger ledger) {
		ReferenceType referenceType = ledger.referenceType();
		String referenceTypeValue = referenceType == null ? "" : referenceType.name();
		String tokenTypeValue = ledger.tokenType() == null ? "" : ledger.tokenType().name();
		return """
				{
				  "memberUuid":"%s",
				  "transactionUuid":"%s",
				  "transactionType":"%s",
				  "tokenType":"%s",
				  "amount":%d,
				  "balanceAfter":%d,
				  "referenceType":"%s",
				  "description":"%s"
				}
				""".formatted(
				ledger.memberUuid(),
				ledger.transactionUuid(),
				ledger.transactionType(),
				tokenTypeValue,
				ledger.amount(),
				ledger.balanceAfter(),
				referenceTypeValue,
				escapeJson(ledger.description())
		).replaceAll("\\s+", " ").trim();
	}

	private static String escapeJson(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
