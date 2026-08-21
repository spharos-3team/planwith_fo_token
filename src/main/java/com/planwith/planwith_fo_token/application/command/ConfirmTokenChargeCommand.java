package com.planwith.planwith_fo_token.application.command;

import com.planwith.planwith_fo_token.domain.model.vo.ChargeUuid;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public record ConfirmTokenChargeCommand(
		MemberUuid memberUuid,
		ChargeUuid chargeUuid,
		String providerPaymentId,
		Long paidAmount
) {
}
