package com.planwith.planwith_fo_token.application.port.in;

import com.planwith.planwith_fo_token.application.command.HandlePaymentCompletedCommand;

public interface HandlePaymentCompletedUseCase {

	void handle(HandlePaymentCompletedCommand command);
}
