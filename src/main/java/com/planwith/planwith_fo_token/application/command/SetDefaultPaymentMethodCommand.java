package com.planwith.planwith_fo_token.application.command;

import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

public record SetDefaultPaymentMethodCommand(
		MemberUuid memberUuid,
		PaymentMethodUuid paymentMethodUuid
) {
}
