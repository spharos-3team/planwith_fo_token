package com.planwith.planwith_fo_token.application.query;

import java.util.UUID;

public record WalletLedgerConsistencyResult(
		UUID memberUuid,
		boolean consistent,
		long walletTotalBalance,
		long ledgerBalanceAfter,
		int ledgerCount
) {
}
