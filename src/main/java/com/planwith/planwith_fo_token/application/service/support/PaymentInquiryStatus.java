package com.planwith.planwith_fo_token.application.service.support;

/**
 * PG 결제 조회 상태를 도메인 처리 단위로 정규화한다.
 */
public enum PaymentInquiryStatus {
	PAID,
	FAILED,
	CANCELED,
	UNKNOWN;

	public static PaymentInquiryStatus from(String rawStatus) {
		if (rawStatus == null || rawStatus.isBlank()) {
			return UNKNOWN;
		}
		return switch (rawStatus.trim().toUpperCase()) {
			case "PAID", "SUCCEEDED", "DONE" -> PAID;
			case "FAILED", "FAILURE", "PAY_FAILED" -> FAILED;
			case "CANCELED", "CANCELLED", "PARTIAL_CANCELLED", "PARTIAL_CANCELED" -> CANCELED;
			default -> UNKNOWN;
		};
	}
}
