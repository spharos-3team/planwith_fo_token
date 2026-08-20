package com.planwith.planwith_fo_token.application.event;

public final class TokenExpiredEvent {

	public static final String EVENT_TYPE = "TokenExpired";
	public static final String AGGREGATE_TYPE = "TokenWallet";

	private TokenExpiredEvent() {
	}
}
