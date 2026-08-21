package com.planwith.planwith_fo_token.application.service.support;

import java.time.Instant;

import com.planwith.planwith_fo_token.application.event.TokenChargeFailedEvent;
import com.planwith.planwith_fo_token.application.port.out.TokenOutboxMessage;
import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;

/**
 * 결제 실패/취소 Outbox 메시지 생성. Kafka 구현체에 의존하지 않는다.
 */
final class TokenChargeFailedOutboxSupport {

	private TokenChargeFailedOutboxSupport() {
	}

	static TokenOutboxMessage toOutboxMessage(TokenCharge charge, String reason, String pgStatus) {
		Instant occurredAt = Instant.now();
		return new TokenOutboxMessage(
				charge.chargeUuid().toString(),
				TokenChargeFailedEvent.AGGREGATE_TYPE,
				charge.chargeUuid().toString(),
				TokenChargeFailedEvent.EVENT_TYPE,
				buildPayload(charge, reason, pgStatus, occurredAt),
				occurredAt
		);
	}

	private static String buildPayload(
			TokenCharge charge,
			String reason,
			String pgStatus,
			Instant occurredAt
	) {
		ChargeStatus status = charge.status();
		return """
				{
				  "chargeUuid":"%s",
				  "memberUuid":"%s",
				  "status":"%s",
				  "reason":"%s",
				  "pgStatus":"%s",
				  "providerPaymentId":"%s",
				  "tokenAmount":%d,
				  "paidAmount":%d,
				  "productCode":"%s",
				  "occurredAt":"%s"
				}
				""".formatted(
				charge.chargeUuid(),
				charge.memberUuid() == null ? "" : charge.memberUuid(),
				status == null ? "" : status.name(),
				escapeJson(reason),
				escapeJson(pgStatus),
				escapeJson(charge.providerPaymentId()),
				charge.tokenAmount(),
				charge.paidAmount(),
				charge.productCode() == null ? "" : charge.productCode().name(),
				occurredAt
		).replaceAll("\\s+", " ").trim();
	}

	private static String escapeJson(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
