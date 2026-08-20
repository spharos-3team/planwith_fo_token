package com.planwith.planwith_fo_token.application.port.in.command;

import com.planwith.planwith_fo_token.application.command.RewardTokenCommand;

public interface RewardTokenUseCase {

	void reward(RewardTokenCommand command);
}
