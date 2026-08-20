package com.planwith.planwith_fo_token.adapter.out.persistence.wallet;

import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

final class TokenWalletPersistenceMapper {

	private TokenWalletPersistenceMapper() {
	}

	static TokenWallet toDomain(TokenWalletJpaEntity entity) {
		return TokenWallet.restore(
				entity.getWalletId(),
				new MemberUuid(entity.getMemberUuid()),
				entity.getBalance(),
				entity.getVersion()
		);
	}

	static void applyToEntity(TokenWallet wallet, TokenWalletJpaEntity entity) {
		entity.setBalance(wallet.balance());
	}
}
