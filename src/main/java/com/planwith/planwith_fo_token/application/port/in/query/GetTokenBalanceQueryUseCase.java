package com.planwith.planwith_fo_token.application.port.in.query;

import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.TokenBalanceResult;

public interface GetTokenBalanceQueryUseCase {

	TokenBalanceResult getBalance(GetTokenBalanceQuery query);
}
