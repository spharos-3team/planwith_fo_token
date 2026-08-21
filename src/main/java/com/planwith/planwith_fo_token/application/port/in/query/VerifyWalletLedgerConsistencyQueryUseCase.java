package com.planwith.planwith_fo_token.application.port.in.query;

import com.planwith.planwith_fo_token.application.query.VerifyWalletLedgerConsistencyQuery;
import com.planwith.planwith_fo_token.application.query.WalletLedgerConsistencyResult;

public interface VerifyWalletLedgerConsistencyQueryUseCase {

	WalletLedgerConsistencyResult verify(VerifyWalletLedgerConsistencyQuery query);
}
