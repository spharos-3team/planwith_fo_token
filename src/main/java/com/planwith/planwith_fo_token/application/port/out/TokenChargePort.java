package com.planwith.planwith_fo_token.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_token.domain.model.TokenCharge;

public interface TokenChargePort {

	TokenCharge save(TokenCharge charge);

	Optional<TokenCharge> findByChargeUuid(UUID chargeUuid);
}
