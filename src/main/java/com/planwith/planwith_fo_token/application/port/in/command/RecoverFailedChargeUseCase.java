package com.planwith.planwith_fo_token.application.port.in.command;

import com.planwith.planwith_fo_token.application.command.RecoverFailedChargeCommand;

public interface RecoverFailedChargeUseCase {

	void recover(RecoverFailedChargeCommand command);
}
