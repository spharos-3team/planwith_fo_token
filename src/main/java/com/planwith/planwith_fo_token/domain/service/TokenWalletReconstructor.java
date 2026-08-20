package com.planwith.planwith_fo_token.domain.service;

import java.util.List;

import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntry;
import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntryType;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public final class TokenWalletReconstructor {

	private TokenWalletReconstructor() {
	}

	public static TokenWallet reconstruct(MemberUuid memberUuid, List<TokenLedgerEntry> chronologicalEntries) {
		TokenWallet wallet = TokenWallet.empty(memberUuid);
		for (TokenLedgerEntry entry : chronologicalEntries) {
			apply(wallet, entry);
		}
		return wallet;
	}

	private static void apply(TokenWallet wallet, TokenLedgerEntry entry) {
		TokenLedgerEntryType type = entry.transactionType();
		if (type == TokenLedgerEntryType.CHARGE || type == TokenLedgerEntryType.REWARD) {
			wallet.credit(TokenPolicy.kindOfGrant(type, entry.referenceType()), entry.amount());
			return;
		}
		if (type == TokenLedgerEntryType.USE) {
			wallet.debit(entry.amount());
			return;
		}
		if (type == TokenLedgerEntryType.EXPIRE) {
			wallet.expireFree();
		}
	}
}
