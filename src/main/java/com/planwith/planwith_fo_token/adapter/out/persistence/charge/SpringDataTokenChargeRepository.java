package com.planwith.planwith_fo_token.adapter.out.persistence.charge;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.planwith.planwith_fo_token.domain.model.ChargeStatus;

interface SpringDataTokenChargeRepository extends JpaRepository<TokenChargeJpaEntity, Long> {

	Optional<TokenChargeJpaEntity> findByChargeUuid(UUID chargeUuid);

	Optional<TokenChargeJpaEntity> findByChargeUuidAndMemberUuid(UUID chargeUuid, UUID memberUuid);

	Optional<TokenChargeJpaEntity> findByMemberUuidAndClientRequestId(UUID memberUuid, String clientRequestId);

	@Query("""
			select charge
			from TokenChargeJpaEntity charge
			where charge.memberUuid = :memberUuid
			   or charge.paymentMethodUuid in (
				select method.paymentMethodUuid
				from PaymentMethodJpaEntity method
				where method.memberUuid = :memberUuid
			)
			   or charge.walletUuid in (
				select ledger.tokenLedgerUuid
				from TokenLedgerJpaEntity ledger
				where ledger.memberUuid = :memberUuid
			)
			order by coalesce(charge.chargedAt, charge.createdAt) desc
			""")
	List<TokenChargeJpaEntity> findByMemberUuidOrderByChargedAtDesc(
			@Param("memberUuid") UUID memberUuid,
			Pageable pageable
	);

	@Query("""
			select charge
			from TokenChargeJpaEntity charge
			where charge.status = :status
			  and charge.createdAt <= :createdBefore
			order by charge.createdAt asc
			""")
	List<TokenChargeJpaEntity> findByStatusAndCreatedAtBefore(
			@Param("status") ChargeStatus status,
			@Param("createdBefore") Instant createdBefore,
			Pageable pageable
	);
}
