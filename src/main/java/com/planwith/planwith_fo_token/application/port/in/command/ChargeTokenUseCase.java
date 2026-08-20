package com.planwith.planwith_fo_token.application.port.in.command;

import com.planwith.planwith_fo_token.application.command.ChargeTokenCommand;

public interface ChargeTokenUseCase {

	void charge(ChargeTokenCommand command);
}
