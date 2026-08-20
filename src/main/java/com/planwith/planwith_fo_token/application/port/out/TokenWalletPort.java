package com.planwith.planwith_fo_token.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public interface TokenWalletPort {

	Optional<TokenWallet> findByMemberUuid(MemberUuid memberUuid);

	TokenWallet save(TokenWallet wallet);
}
