package com.planwith.planwith_fo_token.domain.model.vo;

public record WalletVersion(long value) {

	public WalletVersion {
		if (value < 0) {
			throw new IllegalArgumentException("Wallet version cannot be negative.");
		}
	}
}
