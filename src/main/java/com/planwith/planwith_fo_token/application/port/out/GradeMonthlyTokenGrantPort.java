package com.planwith.planwith_fo_token.application.port.out;

import com.planwith.planwith_fo_token.domain.model.GradeMonthlyTokenGrant;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public interface GradeMonthlyTokenGrantPort {

	boolean existsByMemberUuidAndRewardMonth(MemberUuid memberUuid, String rewardMonth);

	boolean saveIdempotent(GradeMonthlyTokenGrant grant);
}
