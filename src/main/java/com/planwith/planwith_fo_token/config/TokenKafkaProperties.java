package com.planwith.planwith_fo_token.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "token.kafka")
public class TokenKafkaProperties {

	private boolean consumerEnabled = false;
	private Topics topics = new Topics();

	public boolean isConsumerEnabled() {
		return consumerEnabled;
	}

	public void setConsumerEnabled(boolean consumerEnabled) {
		this.consumerEnabled = consumerEnabled;
	}

	public Topics getTopics() {
		return topics;
	}

	public void setTopics(Topics topics) {
		this.topics = topics;
	}

	public static class Topics {
		private String paymentCompleted = "planwith.payment.completed";
		private String gradeRewardGranted = "planwith.grade.reward-granted";
		private String gradeInitialBonusGranted = "planwith.grade.initial-bonus-granted";
		private String tokenCharged = "planwith.token.charged";
		private String tokenUsed = "planwith.token.used";
		private String tokenRewarded = "planwith.token.rewarded";
		private String tokenExpired = "planwith.token.expired";
		private String tokenChargeFailed = "planwith.token.charge-failed";

		public String getPaymentCompleted() { return paymentCompleted; }
		public void setPaymentCompleted(String paymentCompleted) { this.paymentCompleted = paymentCompleted; }
		public String getGradeRewardGranted() { return gradeRewardGranted; }
		public void setGradeRewardGranted(String gradeRewardGranted) { this.gradeRewardGranted = gradeRewardGranted; }
		public String getGradeInitialBonusGranted() { return gradeInitialBonusGranted; }
		public void setGradeInitialBonusGranted(String gradeInitialBonusGranted) {
			this.gradeInitialBonusGranted = gradeInitialBonusGranted;
		}
		public String getTokenCharged() { return tokenCharged; }
		public void setTokenCharged(String tokenCharged) { this.tokenCharged = tokenCharged; }
		public String getTokenUsed() { return tokenUsed; }
		public void setTokenUsed(String tokenUsed) { this.tokenUsed = tokenUsed; }
		public String getTokenRewarded() { return tokenRewarded; }
		public void setTokenRewarded(String tokenRewarded) { this.tokenRewarded = tokenRewarded; }
		public String getTokenExpired() { return tokenExpired; }
		public void setTokenExpired(String tokenExpired) { this.tokenExpired = tokenExpired; }
		public String getTokenChargeFailed() { return tokenChargeFailed; }
		public void setTokenChargeFailed(String tokenChargeFailed) { this.tokenChargeFailed = tokenChargeFailed; }
	}
}
