package com.planwith.planwith_fo_token.adapter.out.persistence.payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_token.domain.model.PaymentMethodStatus;

interface SpringDataPaymentMethodRepository extends JpaRepository<PaymentMethodJpaEntity, Long> {

	Optional<PaymentMethodJpaEntity> findByPaymentMethodUuid(UUID paymentMethodUuid);

	List<PaymentMethodJpaEntity> findByMemberUuidAndStatusOrderByRegisteredAtAsc(
			UUID memberUuid,
			PaymentMethodStatus status
	);

	Optional<PaymentMethodJpaEntity> findByMemberUuidAndDefaultMethodTrueAndStatus(
			UUID memberUuid,
			PaymentMethodStatus status
	);

	Optional<PaymentMethodJpaEntity> findByPaymentMethodUuidAndMemberUuid(UUID paymentMethodUuid, UUID memberUuid);
}
