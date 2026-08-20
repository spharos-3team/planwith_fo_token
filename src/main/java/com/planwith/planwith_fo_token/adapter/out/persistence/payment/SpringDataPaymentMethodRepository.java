package com.planwith.planwith_fo_token.adapter.out.persistence.payment;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataPaymentMethodRepository extends JpaRepository<PaymentMethodJpaEntity, Long> {

	Optional<PaymentMethodJpaEntity> findByPaymentMethodUuid(UUID paymentMethodUuid);
}
