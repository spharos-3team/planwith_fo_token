package com.planwith.planwith_fo_token.domain.service;

import java.util.List;

import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public final class TokenWalletReconstructor {

	private TokenWalletReconstructor() {
	}

	public static TokenWallet reconstruct(MemberUuid memberUuid, List<TokenLedger> chronologicalEntries) {
		TokenWallet wallet = TokenWallet.empty(memberUuid);
		for (TokenLedger ledger : chronologicalEntries) {
			wallet.apply(ledger);
		}
		return wallet;
	}
}
