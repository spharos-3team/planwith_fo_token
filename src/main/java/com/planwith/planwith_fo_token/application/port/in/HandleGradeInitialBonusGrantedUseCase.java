package com.planwith.planwith_fo_token.application.port.in;

import com.planwith.planwith_fo_token.application.command.HandleGradeInitialBonusGrantedCommand;

public interface HandleGradeInitialBonusGrantedUseCase {

	void handle(HandleGradeInitialBonusGrantedCommand command);
}
