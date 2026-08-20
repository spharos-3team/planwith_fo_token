package com.planwith.planwith_fo_token.domain.service;

import java.time.Instant;

import com.planwith.planwith_fo_token.domain.model.TokenKind;
import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntry;
import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntryType;
import com.planwith.planwith_fo_token.domain.model.TokenReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;

public final class TokenLedgerDomainService {

	private TokenLedgerDomainService() {
	}

	public static TokenLedgerEntry grant(
			TokenWallet wallet,
			TokenLedgerEntryType transactionType,
			TokenReferenceType referenceType,
			long amount,
			String description,
			Instant occurredAt
	) {
		TokenKind kind = TokenPolicy.kindOfGrant(transactionType, referenceType);
		wallet.credit(kind, amount);
		return TokenLedgerEntry.append(
				wallet.memberUuid(),
				transactionType,
				amount,
				wallet.totalBalance(),
				referenceType,
				description,
				occurredAt
		);
	}

	public static TokenLedgerEntry use(
			TokenWallet wallet,
			long amount,
			TokenReferenceType referenceType,
			String description,
			Instant occurredAt
	) {
		wallet.debit(amount);
		return TokenLedgerEntry.append(
				wallet.memberUuid(),
				TokenLedgerEntryType.USE,
				amount,
				wallet.totalBalance(),
				referenceType,
				description,
				occurredAt
		);
	}

	public static TokenLedgerEntry expireFree(TokenWallet wallet, Instant occurredAt) {
		long expired = wallet.expireFree();
		return TokenLedgerEntry.append(
				wallet.memberUuid(),
				TokenLedgerEntryType.EXPIRE,
				expired,
				wallet.totalBalance(),
				TokenReferenceType.GRADE_REWARD,
				"FREE token monthly expiry",
				occurredAt
		);
	}
}
