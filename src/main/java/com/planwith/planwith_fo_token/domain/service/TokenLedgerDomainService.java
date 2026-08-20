package com.planwith.planwith_fo_token.domain.service;

import java.time.Instant;

import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.TransactionType;

public final class TokenLedgerDomainService {

	private TokenLedgerDomainService() {
	}

	public static TokenLedger grant(
			TokenWallet wallet,
			TransactionType transactionType,
			ReferenceType referenceType,
			long amount,
			String description,
			Instant occurredAt
	) {
		return wallet.grant(transactionType, referenceType, amount, description, occurredAt);
	}

	public static TokenLedger use(
			TokenWallet wallet,
			long amount,
			ReferenceType referenceType,
			String description,
			Instant occurredAt
	) {
		return wallet.use(amount, referenceType, description, occurredAt);
	}

	public static TokenLedger expire(TokenWallet wallet, Instant occurredAt) {
		return wallet.expire(occurredAt);
	}
}
