package com.planwith.planwith_fo_token.application.port.in.command;

import com.planwith.planwith_fo_token.application.command.RegisterPaymentMethodCommand;
import com.planwith.planwith_fo_token.application.query.PaymentMethodResult;

public interface RegisterPaymentMethodUseCase {

	PaymentMethodResult register(RegisterPaymentMethodCommand command);
}
