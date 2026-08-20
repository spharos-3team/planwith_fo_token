package com.planwith.planwith_fo_token.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

public interface PaymentMethodPort {

	PaymentMethod save(PaymentMethod paymentMethod);

	Optional<PaymentMethod> findByUuid(UUID paymentMethodUuid);

	List<PaymentMethod> findActiveByMemberUuid(MemberUuid memberUuid);

	Optional<PaymentMethod> findDefaultActiveByMemberUuid(MemberUuid memberUuid);

	Optional<PaymentMethod> findByUuidAndMemberUuid(PaymentMethodUuid paymentMethodUuid, MemberUuid memberUuid);
}
