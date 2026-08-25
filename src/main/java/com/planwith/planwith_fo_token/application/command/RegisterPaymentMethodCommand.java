package com.planwith.planwith_fo_token.application.command;

import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public record RegisterPaymentMethodCommand(
		MemberUuid memberUuid,
		String cardName,
		String cardNumber,
		String expiryYear,
		String expiryMonth,
		String birthOrBusinessRegistrationNumber,
		String passwordTwoDigits,
		boolean defaultMethod
) {
	public RegisterPaymentMethodCommand {
		if (cardName == null || cardName.isBlank()) {
			throw new IllegalArgumentException("Card name is required.");
		}
		cardName = cardName.trim();
		if (cardName.length() > 100) {
			throw new IllegalArgumentException("Card name must be 100 characters or fewer.");
		}
	}
}
