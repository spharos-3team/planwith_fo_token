package com.planwith.planwith_fo_token.adapter.out.persistence.charge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.TokenChargePort;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

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
		TokenChargeJpaEntity entity;
		if (charge.chargeId() != null) {
			entity = repository.findById(charge.chargeId())
					.orElseGet(() -> TokenChargePersistenceMapper.toEntity(charge));
			entity.updateMutableState(
					charge.walletUuid() == null ? null : charge.walletUuid().value(),
					charge.providerPaymentId(),
					charge.status(),
					charge.chargedAt()
			);
		} else {
			entity = TokenChargePersistenceMapper.toEntity(charge);
		}
		TokenChargeJpaEntity saved = repository.save(entity);
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

	@Override
	@Transactional(readOnly = true)
	public Optional<TokenCharge> findByChargeUuidAndMemberUuid(UUID chargeUuid, MemberUuid memberUuid) {
		return repository.findByChargeUuidAndMemberUuid(chargeUuid, memberUuid.value())
				.map(TokenChargePersistenceMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<TokenCharge> findByMemberUuidAndClientRequestId(MemberUuid memberUuid, String clientRequestId) {
		if (clientRequestId == null || clientRequestId.isBlank()) {
			return Optional.empty();
		}
		return repository.findByMemberUuidAndClientRequestId(memberUuid.value(), clientRequestId)
				.map(TokenChargePersistenceMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TokenCharge> findByMemberUuid(MemberUuid memberUuid, int page, int size) {
		return repository.findByMemberUuidOrderByChargedAtDesc(
						memberUuid.value(),
						PageRequest.of(normalizePage(page), normalizeSize(size))
				)
				.stream()
				.map(TokenChargePersistenceMapper::toDomain)
				.toList();
	}

	private static int normalizePage(int page) {
		return Math.max(page, 0);
	}

	private static int normalizeSize(int size) {
		return size > 0 ? size : 20;
	}
}
