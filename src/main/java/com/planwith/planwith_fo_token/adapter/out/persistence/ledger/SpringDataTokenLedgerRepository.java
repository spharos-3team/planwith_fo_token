package com.planwith.planwith_fo_token.adapter.out.persistence.ledger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntryType;

interface SpringDataTokenLedgerRepository extends JpaRepository<TokenLedgerJpaEntity, Long> {

	boolean existsByTransactionUuid(UUID transactionUuid);

	Optional<TokenLedgerJpaEntity> findByTransactionUuid(UUID transactionUuid);

	List<TokenLedgerJpaEntity> findByMemberUuidOrderByOccurredAtDesc(UUID memberUuid, Pageable pageable);

	List<TokenLedgerJpaEntity> findByMemberUuidAndEntryTypeOrderByOccurredAtDesc(
			UUID memberUuid,
			TokenLedgerEntryType entryType,
			Pageable pageable
	);
}
