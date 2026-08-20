package com.planwith.planwith_fo_token.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.event.TokenRewardedEvent;
import com.planwith.planwith_fo_token.application.port.out.TokenEventOutboxPort;
import com.planwith.planwith_fo_token.application.port.out.TokenOutboxMessage;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TokenOutboxAdapterIntegrationTest {

	@Autowired
	private TokenEventOutboxPort tokenEventOutboxPort;

	@Autowired
	private SpringDataTokenOutboxRepository repository;

	@Test
	void saveOutboxMessage() {
		tokenEventOutboxPort.save(new TokenOutboxMessage(
				"33333333-3333-3333-3333-333333333333",
				TokenRewardedEvent.AGGREGATE_TYPE,
				"22222222-2222-2222-2222-222222222222",
				TokenRewardedEvent.EVENT_TYPE,
				"{\"tokenAmount\":100}"
		));

		assertThat(repository.existsByEventUuid(
				java.util.UUID.fromString("33333333-3333-3333-3333-333333333333")
		)).isTrue();
	}
}
