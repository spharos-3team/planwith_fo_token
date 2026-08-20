package com.planwith.planwith_fo_token.application.port.in.command;

import com.planwith.planwith_fo_token.application.command.ExpireTokenCommand;

public interface ExpireTokenUseCase {

	void expire(ExpireTokenCommand command);
}
