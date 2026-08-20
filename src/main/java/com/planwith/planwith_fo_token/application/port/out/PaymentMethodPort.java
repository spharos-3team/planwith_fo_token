package com.planwith.planwith_fo_token.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_token.domain.model.PaymentMethod;

public interface PaymentMethodPort {

	PaymentMethod save(PaymentMethod paymentMethod);

	Optional<PaymentMethod> findByUuid(UUID paymentMethodUuid);
}
