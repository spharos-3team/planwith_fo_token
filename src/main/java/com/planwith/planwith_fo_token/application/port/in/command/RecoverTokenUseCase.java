package com.planwith.planwith_fo_token.application.port.in.command;

import com.planwith.planwith_fo_token.application.command.RecoverTokenCommand;

public interface RecoverTokenUseCase {

	void recover(RecoverTokenCommand command);
}
