package com.planwith.planwith_fo_token.adapter.out.persistence.ledger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_token.domain.model.TransactionType;

interface SpringDataTokenLedgerRepository extends JpaRepository<TokenLedgerJpaEntity, Long> {

	boolean existsByTokenLedgerUuid(UUID tokenLedgerUuid);

	Optional<TokenLedgerJpaEntity> findByTokenLedgerUuid(UUID tokenLedgerUuid);

	List<TokenLedgerJpaEntity> findByMemberUuidOrderByOccurredAtAsc(UUID memberUuid);

	List<TokenLedgerJpaEntity> findByMemberUuidOrderByOccurredAtDesc(UUID memberUuid, Pageable pageable);

	List<TokenLedgerJpaEntity> findByMemberUuidAndTransactionTypeOrderByOccurredAtDesc(
			UUID memberUuid,
			TransactionType transactionType,
			Pageable pageable
	);
}
