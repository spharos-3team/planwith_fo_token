package com.planwith.planwith_fo_token.domain.service;

import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntry;
import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntryType;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;

public final class TokenLedgerDomainService {

	private TokenLedgerDomainService() {
	}

	public static TokenLedgerEntry applyCredit(TokenWallet wallet, long amount, TokenLedgerEntryType entryType) {
		return wallet.credit(amount, entryType);
	}

	public static TokenLedgerEntry applyDebit(TokenWallet wallet, long amount, TokenLedgerEntryType entryType) {
		return wallet.debit(amount, entryType);
	}
}
