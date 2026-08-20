package com.planwith.planwith_fo_token.application.port.out;

import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public interface TokenWalletPort {

	TokenWallet getByMemberUuid(MemberUuid memberUuid);
}
