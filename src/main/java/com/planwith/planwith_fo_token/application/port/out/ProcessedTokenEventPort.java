package com.planwith.planwith_fo_token.application.port.out;

import java.util.UUID;

import com.planwith.planwith_fo_token.domain.model.ProcessedTokenEvent;

public interface ProcessedTokenEventPort {

	boolean existsByEventUuid(UUID eventUuid);

	void save(ProcessedTokenEvent event);
}
