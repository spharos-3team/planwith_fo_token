package com.planwith.planwith_fo_token.application.port.out;

import com.planwith.planwith_fo_token.domain.model.TokenLedger;

/**
 * Ledger Pattern: wallet state changes are persisted by appending immutable ledger entries.
 */
public interface SaveTokenWalletPort {

	TokenLedger saveMutation(TokenLedger ledger);
}
