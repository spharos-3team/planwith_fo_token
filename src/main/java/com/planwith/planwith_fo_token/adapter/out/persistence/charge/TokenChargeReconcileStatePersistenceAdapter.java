package com.planwith.planwith_fo_token.adapter.out.persistence.charge;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.TokenChargeReconcileStatePort;

@Component
public class TokenChargeReconcileStatePersistenceAdapter implements TokenChargeReconcileStatePort {

	private final SpringDataTokenChargeReconcileStateRepository repository;

	public TokenChargeReconcileStatePersistenceAdapter(SpringDataTokenChargeReconcileStateRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public int currentRetryCount(UUID chargeUuid) {
		return repository.findByChargeUuid(chargeUuid)
				.map(TokenChargeReconcileStateJpaEntity::retryCount)
				.orElse(0);
	}

	@Override
	@Transactional
	public void markSucceeded(UUID chargeUuid, Instant attemptedAt) {
		TokenChargeReconcileStateJpaEntity entity = repository.findByChargeUuid(chargeUuid)
				.orElseGet(() -> TokenChargeReconcileStateJpaEntity.create(chargeUuid));
		entity.markSucceeded(attemptedAt);
		repository.save(entity);
	}

	@Override
	@Transactional
	public void markFailed(UUID chargeUuid, String result, Instant attemptedAt, Instant nextRetryAt) {
		TokenChargeReconcileStateJpaEntity entity = repository.findByChargeUuid(chargeUuid)
				.orElseGet(() -> TokenChargeReconcileStateJpaEntity.create(chargeUuid));
		entity.recordAttempt(result, attemptedAt, nextRetryAt);
		repository.save(entity);
	}
}
