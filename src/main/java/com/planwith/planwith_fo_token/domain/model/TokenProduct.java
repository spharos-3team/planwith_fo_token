package com.planwith.planwith_fo_token.domain.model;

import java.util.Objects;

/**
 * 서버가 통제하는 토큰 상품 스냅샷.
 */
public final class TokenProduct {

	private final TokenProductCode code;
	private final String name;
	private final long salePrice;
	private final long baseTokenAmount;
	private final long bonusTokenAmount;

	public TokenProduct(
			TokenProductCode code,
			String name,
			long salePrice,
			long baseTokenAmount,
			long bonusTokenAmount
	) {
		this.code = Objects.requireNonNull(code, "Product code is required.");
		this.name = Objects.requireNonNull(name, "Product name is required.");
		if (salePrice <= 0) {
			throw new IllegalArgumentException("Sale price must be positive.");
		}
		if (baseTokenAmount <= 0) {
			throw new IllegalArgumentException("Base token amount must be positive.");
		}
		if (bonusTokenAmount < 0) {
			throw new IllegalArgumentException("Bonus token amount must not be negative.");
		}
		this.salePrice = salePrice;
		this.baseTokenAmount = baseTokenAmount;
		this.bonusTokenAmount = bonusTokenAmount;
	}

	public TokenProductCode code() {
		return code;
	}

	public String name() {
		return name;
	}

	public long salePrice() {
		return salePrice;
	}

	public long baseTokenAmount() {
		return baseTokenAmount;
	}

	public long bonusTokenAmount() {
		return bonusTokenAmount;
	}

	public long totalTokenAmount() {
		return baseTokenAmount + bonusTokenAmount;
	}
}
