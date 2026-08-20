package com.planwith.planwith_fo_token.application.service.support;

import com.planwith.planwith_fo_token.domain.model.ReferenceType;

public final class TokenCommandSupport {

	private TokenCommandSupport() {
	}

	public static ReferenceType parseReferenceType(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return ReferenceType.valueOf(value);
	}

	public static String descriptionOrDefault(String description, String defaultDescription) {
		if (description == null || description.isBlank()) {
			return defaultDescription;
		}
		return description;
	}
}
