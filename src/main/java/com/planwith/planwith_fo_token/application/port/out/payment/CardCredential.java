package com.planwith.planwith_fo_token.application.port.out.payment;

import java.time.Instant;

public record CardCredential(
		String number,
		String expiryYear,
		String expiryMonth,
		String birthOrBusinessRegistrationNumber,
		String passwordTwoDigits
) {
}
