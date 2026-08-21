package com.planwith.planwith_fo_token.application.command;

import com.planwith.planwith_fo_token.domain.model.PaymentType;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

public record RequestTokenChargeCommand(
		MemberUuid memberUuid,
		String productCode,
		PaymentMethodUuid paymentMethodUuid,
		PaymentType paymentType,
		String clientRequestId
) {
}
