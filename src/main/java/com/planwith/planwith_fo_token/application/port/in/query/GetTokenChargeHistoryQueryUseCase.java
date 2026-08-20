package com.planwith.planwith_fo_token.application.port.in.query;

import java.util.List;

import com.planwith.planwith_fo_token.application.query.GetTokenChargeHistoryQuery;
import com.planwith.planwith_fo_token.application.query.TokenChargeHistoryResult;

public interface GetTokenChargeHistoryQueryUseCase {

	List<TokenChargeHistoryResult> getChargeHistory(GetTokenChargeHistoryQuery query);
}
