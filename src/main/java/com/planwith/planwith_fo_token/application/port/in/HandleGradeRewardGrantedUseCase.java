package com.planwith.planwith_fo_token.application.port.in;

import com.planwith.planwith_fo_token.application.command.HandleGradeRewardGrantedCommand;

public interface HandleGradeRewardGrantedUseCase {

	void handle(HandleGradeRewardGrantedCommand command);
}
