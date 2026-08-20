package com.planwith.planwith_fo_token.adapter.out.persistence.wallet;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

interface SpringDataTokenWalletStateRepository extends JpaRepository<TokenWalletStateJpaEntity, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT state FROM TokenWalletStateJpaEntity state WHERE state.memberUuid = :memberUuid")
	Optional<TokenWalletStateJpaEntity> findByMemberUuidForUpdate(@Param("memberUuid") UUID memberUuid);
}
