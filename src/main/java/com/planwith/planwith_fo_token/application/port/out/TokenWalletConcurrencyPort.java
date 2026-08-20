package com.planwith.planwith_fo_token.application.port.out;

import java.util.function.Supplier;

import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public interface TokenWalletConcurrencyPort {

	<T> T executeWithMemberLock(MemberUuid memberUuid, Supplier<T> action);
}
