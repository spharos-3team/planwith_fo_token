package com.planwith.planwith_fo_token.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "token.charge.reconcile")
public class TokenChargeReconcileProperties {

	private boolean enabled = false;
	private int batchSize = 50;
	private Duration staleAfter = Duration.ofMinutes(5);
	private Duration interval = Duration.ofMinutes(1);
	private Duration initialDelay = Duration.ofSeconds(30);
	private int maxRetry = 10;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public Duration getStaleAfter() {
		return staleAfter;
	}

	public void setStaleAfter(Duration staleAfter) {
		this.staleAfter = staleAfter;
	}

	public Duration getInterval() {
		return interval;
	}

	public void setInterval(Duration interval) {
		this.interval = interval;
	}

	public Duration getInitialDelay() {
		return initialDelay;
	}

	public void setInitialDelay(Duration initialDelay) {
		this.initialDelay = initialDelay;
	}

	public int getMaxRetry() {
		return maxRetry;
	}

	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}

	public int resolvedBatchSize() {
		return batchSize > 0 ? batchSize : 50;
	}

	public Duration resolvedStaleAfter() {
		if (staleAfter == null || staleAfter.isZero() || staleAfter.isNegative()) {
			return Duration.ofMinutes(5);
		}
		return staleAfter;
	}

	public int resolvedMaxRetry() {
		return maxRetry > 0 ? maxRetry : 10;
	}
}
