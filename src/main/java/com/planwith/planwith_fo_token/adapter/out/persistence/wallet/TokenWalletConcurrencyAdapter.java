package com.planwith.planwith_fo_token.adapter.out.persistence.wallet;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.TokenWalletConcurrencyPort;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

@Component
public class TokenWalletConcurrencyAdapter implements TokenWalletConcurrencyPort {

	private static final Logger log = LoggerFactory.getLogger(TokenWalletConcurrencyAdapter.class);

	private static final int LOCK_TIMEOUT_MS = 10_000;

	private final SpringDataTokenWalletStateRepository repository;
	private final TokenWalletStateInitializer walletStateInitializer;
	private final EntityManager entityManager;

	public TokenWalletConcurrencyAdapter(
			SpringDataTokenWalletStateRepository repository,
			TokenWalletStateInitializer walletStateInitializer,
			EntityManager entityManager
	) {
		this.repository = repository;
		this.walletStateInitializer = walletStateInitializer;
		this.entityManager = entityManager;
	}

	@Override
	@Transactional
	public <T> T executeWithMemberLock(MemberUuid memberUuid, Supplier<T> action) {
		walletStateInitializer.ensureExists(memberUuid);
		TokenWalletStateJpaEntity state = lockWalletState(memberUuid);
		log.debug(
				"TokenWalletConcurrencyAdapter : executeWithMemberLock : 회원 Wallet 잠금 - memberUuid={}, version={}",
				memberUuid,
				state.getVersion()
		);
		T result = action.get();
		state.touch(Instant.now());
		repository.save(state);
		return result;
	}

	private TokenWalletStateJpaEntity lockWalletState(MemberUuid memberUuid) {
		TokenWalletStateJpaEntity state = repository.findById(memberUuid.value())
				.orElseThrow(() -> new IllegalStateException(
						"Failed to lock wallet state for memberUuid=" + memberUuid
				));
		if (entityManager.getLockMode(state) != LockModeType.PESSIMISTIC_WRITE) {
			Map<String, Object> hints = new HashMap<>();
			hints.put("jakarta.persistence.lock.timeout", LOCK_TIMEOUT_MS);
			entityManager.lock(state, LockModeType.PESSIMISTIC_WRITE, hints);
		}
		return state;
	}
}
