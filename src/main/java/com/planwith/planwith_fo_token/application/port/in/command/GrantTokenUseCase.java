package com.planwith.planwith_fo_token.application.port.in.command;

import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;

public interface GrantTokenUseCase {

	void grant(GrantTokenCommand command);
}
