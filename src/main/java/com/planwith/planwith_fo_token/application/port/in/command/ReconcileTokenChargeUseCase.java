package com.planwith.planwith_fo_token.application.port.in.command;

import com.planwith.planwith_fo_token.application.command.ReconcileTokenChargeCommand;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;

public interface ReconcileTokenChargeUseCase {

	TokenChargeRequestResult reconcile(ReconcileTokenChargeCommand command);

	int reconcileStaleReadyCharges();
}
