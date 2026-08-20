package com.planwith.planwith_fo_token.adapter.out.persistence.wallet;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataTokenWalletRepository extends JpaRepository<TokenWalletJpaEntity, Long> {

	Optional<TokenWalletJpaEntity> findByMemberUuid(UUID memberUuid);
}
