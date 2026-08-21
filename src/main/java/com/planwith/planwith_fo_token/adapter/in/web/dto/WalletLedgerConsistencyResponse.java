package com.planwith.planwith_fo_token.adapter.in.web.dto;

import java.util.UUID;

public record WalletLedgerConsistencyResponse(
		UUID memberUuid,
		boolean consistent,
		long walletTotalBalance,
		long ledgerBalanceAfter,
		int ledgerCount
) {
}
