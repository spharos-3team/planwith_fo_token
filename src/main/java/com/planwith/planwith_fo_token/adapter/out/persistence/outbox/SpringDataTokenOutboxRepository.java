package com.planwith.planwith_fo_token.adapter.out.persistence.outbox;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataTokenOutboxRepository extends JpaRepository<TokenOutboxJpaEntity, Long> {

	boolean existsByEventUuid(UUID eventUuid);

	@Query("""
			select outbox
			from TokenOutboxJpaEntity outbox
			where outbox.publishedAt is null
				and (outbox.nextRetryAt is null or outbox.nextRetryAt <= :now)
			order by outbox.occurredAt asc
			""")
	List<TokenOutboxJpaEntity> findDueUnpublished(@Param("now") Instant now, Pageable pageable);
}
