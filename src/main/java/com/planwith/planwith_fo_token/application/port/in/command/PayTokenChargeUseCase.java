package com.planwith.planwith_fo_token.application.port.in.command;

import com.planwith.planwith_fo_token.application.command.PayTokenChargeCommand;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;

public interface PayTokenChargeUseCase {

	TokenChargeRequestResult pay(PayTokenChargeCommand command);
}
