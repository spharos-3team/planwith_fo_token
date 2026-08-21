package com.planwith.planwith_fo_token.application.port.in.command;

import com.planwith.planwith_fo_token.application.command.ConfirmTokenChargeCommand;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;

public interface ConfirmTokenChargeUseCase {

	TokenChargeRequestResult confirm(ConfirmTokenChargeCommand command);
}
