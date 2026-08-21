package com.planwith.planwith_fo_token.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface TokenChargeReconcileStatePort {

	int currentRetryCount(UUID chargeUuid);

	void markSucceeded(UUID chargeUuid, Instant attemptedAt);

	void markFailed(UUID chargeUuid, String result, Instant attemptedAt, Instant nextRetryAt);
}
