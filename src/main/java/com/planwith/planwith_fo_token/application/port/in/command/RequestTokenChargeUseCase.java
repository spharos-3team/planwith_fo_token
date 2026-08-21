package com.planwith.planwith_fo_token.application.port.in.command;

import com.planwith.planwith_fo_token.application.command.RequestTokenChargeCommand;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;

public interface RequestTokenChargeUseCase {

	TokenChargeRequestResult request(RequestTokenChargeCommand command);
}
