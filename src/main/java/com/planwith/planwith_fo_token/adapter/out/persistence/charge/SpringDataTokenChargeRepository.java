package com.planwith.planwith_fo_token.adapter.out.persistence.charge;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataTokenChargeRepository extends JpaRepository<TokenChargeJpaEntity, Long> {

	Optional<TokenChargeJpaEntity> findByChargeUuid(UUID chargeUuid);
}
