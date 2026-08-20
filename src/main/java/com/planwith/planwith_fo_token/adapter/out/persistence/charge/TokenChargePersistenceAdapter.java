package com.planwith.planwith_fo_token.adapter.out.persistence.charge;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.TokenChargePort;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;

@Component
public class TokenChargePersistenceAdapter implements TokenChargePort {

	private static final Logger log = LoggerFactory.getLogger(TokenChargePersistenceAdapter.class);

	private final SpringDataTokenChargeRepository repository;

	public TokenChargePersistenceAdapter(SpringDataTokenChargeRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public TokenCharge save(TokenCharge charge) {
		TokenChargeJpaEntity saved = repository.save(TokenChargePersistenceMapper.toEntity(charge));
		log.debug("TokenChargePersistenceAdapter : save : 충전 요청 저장 - chargeUuid={}, status={}",
				charge.chargeUuid(), charge.status());
		return TokenChargePersistenceMapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<TokenCharge> findByChargeUuid(UUID chargeUuid) {
		return repository.findByChargeUuid(chargeUuid)
				.map(TokenChargePersistenceMapper::toDomain);
	}
}
