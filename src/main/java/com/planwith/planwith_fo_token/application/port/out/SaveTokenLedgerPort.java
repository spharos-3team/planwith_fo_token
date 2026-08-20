package com.planwith.planwith_fo_token.application.port.out;

import com.planwith.planwith_fo_token.domain.model.TokenLedger;

public interface SaveTokenLedgerPort {

	TokenLedger save(TokenLedger ledger);
}
