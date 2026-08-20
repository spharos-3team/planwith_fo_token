package com.planwith.planwith_fo_token.adapter.out.persistence.wallet;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.TokenWalletPort;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@Component
public class TokenWalletPersistenceAdapter implements TokenWalletPort {

	private static final Logger log = LoggerFactory.getLogger(TokenWalletPersistenceAdapter.class);

	private final SpringDataTokenWalletRepository repository;

	public TokenWalletPersistenceAdapter(SpringDataTokenWalletRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<TokenWallet> findByMemberUuid(MemberUuid memberUuid) {
		return repository.findByMemberUuid(memberUuid.value())
				.map(TokenWalletPersistenceMapper::toDomain);
	}

	@Override
	@Transactional
	public TokenWallet save(TokenWallet wallet) {
		TokenWalletJpaEntity entity = repository.findByMemberUuid(wallet.memberUuid().value())
				.orElseGet(() -> TokenWalletJpaEntity.create(wallet.memberUuid().value()));
		TokenWalletPersistenceMapper.applyToEntity(wallet, entity);
		TokenWalletJpaEntity saved = repository.save(entity);
		log.debug("TokenWalletPersistenceAdapter : save : 토큰 지갑 저장 - memberUuid={}, balance={}",
				wallet.memberUuid(), wallet.balance());
		return TokenWalletPersistenceMapper.toDomain(saved);
	}
}
