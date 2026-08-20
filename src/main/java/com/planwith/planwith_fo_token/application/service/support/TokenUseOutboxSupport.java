package com.planwith.planwith_fo_token.application.service.support;

import com.planwith.planwith_fo_token.application.event.TokenUsedEvent;
import com.planwith.planwith_fo_token.application.port.out.TokenEventOutboxPort;
import com.planwith.planwith_fo_token.application.port.out.TokenOutboxMessage;
import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;

final class TokenUseOutboxSupport {

	private TokenUseOutboxSupport() {
	}

	static void saveUseOutbox(TokenEventOutboxPort tokenEventOutboxPort, TokenLedger ledger) {
		tokenEventOutboxPort.save(toOutboxMessage(ledger));
	}

	static TokenOutboxMessage toOutboxMessage(TokenLedger ledger) {
		return new TokenOutboxMessage(
				ledger.transactionUuid().toString(),
				TokenUsedEvent.AGGREGATE_TYPE,
				ledger.memberUuid().toString(),
				TokenUsedEvent.EVENT_TYPE,
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
				  "amount":%d,
				  "balanceAfter":%d,
				  "referenceType":"%s",
				  "description":"%s"
				}
				""".formatted(
				ledger.memberUuid(),
				ledger.transactionUuid(),
				ledger.transactionType(),
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
