package com.planwith.planwith_fo_token.adapter.out.persistence.processed;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataProcessedTokenEventRepository extends JpaRepository<ProcessedTokenEventJpaEntity, Long> {

	boolean existsByEventUuid(UUID eventUuid);
}
