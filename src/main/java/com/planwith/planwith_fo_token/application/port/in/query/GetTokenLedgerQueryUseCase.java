package com.planwith.planwith_fo_token.application.port.in.query;

import java.util.List;

import com.planwith.planwith_fo_token.application.query.GetTokenLedgerQuery;
import com.planwith.planwith_fo_token.application.query.TokenLedgerEntryResult;

public interface GetTokenLedgerQueryUseCase {

	List<TokenLedgerEntryResult> getLedger(GetTokenLedgerQuery query);
}
