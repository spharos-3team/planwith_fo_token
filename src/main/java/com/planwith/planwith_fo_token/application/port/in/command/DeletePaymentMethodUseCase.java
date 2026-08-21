package com.planwith.planwith_fo_token.application.port.in.command;

import com.planwith.planwith_fo_token.application.command.DeletePaymentMethodCommand;

public interface DeletePaymentMethodUseCase {

	void delete(DeletePaymentMethodCommand command);
}
