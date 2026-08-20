package com.planwith.planwith_fo_token.application.port.out;

public interface TokenEventOutboxPort {

	void save(TokenOutboxMessage message);
}
