package com.planwith.planwith_fo_token.application.command;

import com.planwith.planwith_fo_token.domain.model.vo.ChargeUuid;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public record PayTokenChargeCommand(
		MemberUuid memberUuid,
		ChargeUuid chargeUuid,
		Long paidAmount
) {
}
