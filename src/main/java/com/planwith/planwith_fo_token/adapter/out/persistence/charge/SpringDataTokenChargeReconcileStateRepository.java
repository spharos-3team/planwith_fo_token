package com.planwith.planwith_fo_token.adapter.out.persistence.charge;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataTokenChargeReconcileStateRepository
		extends JpaRepository<TokenChargeReconcileStateJpaEntity, Long> {

	Optional<TokenChargeReconcileStateJpaEntity> findByChargeUuid(UUID chargeUuid);
}
