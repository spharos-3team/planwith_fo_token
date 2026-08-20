package com.planwith.planwith_fo_token.adapter.out.persistence.processed;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.ProcessedTokenEventPort;
import com.planwith.planwith_fo_token.domain.model.ProcessedTokenEvent;

@Component
public class ProcessedTokenEventPersistenceAdapter implements ProcessedTokenEventPort {

	private final SpringDataProcessedTokenEventRepository repository;

	public ProcessedTokenEventPersistenceAdapter(SpringDataProcessedTokenEventRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByEventUuid(java.util.UUID eventUuid) {
		return repository.existsByEventUuid(eventUuid);
	}

	@Override
	@Transactional
	public void save(ProcessedTokenEvent event) {
		repository.save(ProcessedTokenEventJpaEntity.create(
				event.eventUuid(),
				event.memberUuid().value(),
				event.eventType(),
				event.processedAt()
		));
	}
}
