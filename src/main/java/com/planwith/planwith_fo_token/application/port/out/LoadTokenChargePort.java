package com.planwith.planwith_fo_token.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_token.domain.model.TokenCharge;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

public interface LoadTokenChargePort {

	Optional<TokenCharge> findByChargeUuid(UUID chargeUuid);

	List<TokenCharge> findByMemberUuid(MemberUuid memberUuid, int page, int size);
}
