package com.planwith.planwith_fo_token.application.command;

import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public record RegisterPaymentMethodCommand(
		MemberUuid memberUuid,
		String cardNumber,
		String expiryYear,
		String expiryMonth,
		String birthOrBusinessRegistrationNumber,
		String passwordTwoDigits,
		boolean defaultMethod
) {
}
