package com.planwith.planwith_fo_token.application.service.support;

import com.planwith.planwith_fo_token.application.event.TokenChargedEvent;
import com.planwith.planwith_fo_token.application.event.TokenRewardedEvent;
import com.planwith.planwith_fo_token.application.port.out.TokenEventOutboxPort;
import com.planwith.planwith_fo_token.application.port.out.TokenOutboxMessage;
import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TransactionType;

final class TokenGrantOutboxSupport {

	private TokenGrantOutboxSupport() {
	}

	static void saveGrantOutbox(TokenEventOutboxPort tokenEventOutboxPort, TokenLedger ledger) {
		tokenEventOutboxPort.save(toOutboxMessage(ledger));
	}

	static TokenOutboxMessage toOutboxMessage(TokenLedger ledger) {
		String eventType = ledger.transactionType() == TransactionType.CHARGE
				? TokenChargedEvent.EVENT_TYPE
				: TokenRewardedEvent.EVENT_TYPE;
		String aggregateType = ledger.transactionType() == TransactionType.CHARGE
				? TokenChargedEvent.AGGREGATE_TYPE
				: TokenRewardedEvent.AGGREGATE_TYPE;
		return new TokenOutboxMessage(
				ledger.transactionUuid().toString(),
				aggregateType,
				ledger.memberUuid().toString(),
				eventType,
				buildPayload(ledger)
		);
	}

	private static String buildPayload(TokenLedger ledger) {
		ReferenceType referenceType = ledger.referenceType();
		String referenceTypeValue = referenceType == null ? "" : referenceType.name();
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
				ledger.tokenType(),
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
