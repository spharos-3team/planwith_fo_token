package com.planwith.planwith_fo_token.adapter.out.persistence.outbox;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.TokenEventOutboxPort;
import com.planwith.planwith_fo_token.application.port.out.TokenOutboxMessage;

@Component
public class TokenEventOutboxAdapter implements TokenEventOutboxPort {

	private static final Logger log = LoggerFactory.getLogger(TokenEventOutboxAdapter.class);

	private final SpringDataTokenOutboxRepository repository;

	public TokenEventOutboxAdapter(SpringDataTokenOutboxRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void save(TokenOutboxMessage message) {
		UUID eventUuid = UUID.fromString(message.eventUuid());
		if (repository.existsByEventUuid(eventUuid)) {
			log.warn("TokenEventOutboxAdapter : save : 중복 Outbox 이벤트 저장 생략 - eventUuid={}",
					message.eventUuid());
			return;
		}
		repository.save(new TokenOutboxJpaEntity(
				eventUuid,
				message.aggregateType(),
				UUID.fromString(message.aggregateUuid()),
				message.eventType(),
				message.payload(),
				message.occurredAt()
		));
		log.info("TokenEventOutboxAdapter : save : 토큰 Outbox 저장 완료 - eventUuid={}, eventType={}",
				message.eventUuid(), message.eventType());
	}
}
