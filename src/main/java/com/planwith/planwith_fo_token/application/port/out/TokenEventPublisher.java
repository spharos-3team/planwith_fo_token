package com.planwith.planwith_fo_token.application.port.out;

import java.util.concurrent.CompletableFuture;

public interface TokenEventPublisher {

	CompletableFuture<Void> publish(String topic, String key, String payload);
}
