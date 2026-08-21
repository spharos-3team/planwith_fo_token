package com.planwith.planwith_fo_token.application.port.in.command;

import com.planwith.planwith_fo_token.application.command.SetDefaultPaymentMethodCommand;
import com.planwith.planwith_fo_token.application.query.PaymentMethodResult;

public interface SetDefaultPaymentMethodUseCase {

	PaymentMethodResult setDefault(SetDefaultPaymentMethodCommand command);
}
