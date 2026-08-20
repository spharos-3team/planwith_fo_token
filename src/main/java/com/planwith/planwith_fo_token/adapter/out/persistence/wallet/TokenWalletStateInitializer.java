package com.planwith.planwith_fo_token.adapter.out.persistence.wallet;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@Component
class TokenWalletStateInitializer {

	private static final Logger log = LoggerFactory.getLogger(TokenWalletStateInitializer.class);

	private final SpringDataTokenWalletStateRepository repository;

	TokenWalletStateInitializer(SpringDataTokenWalletStateRepository repository) {
		this.repository = repository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void ensureExists(MemberUuid memberUuid) {
		synchronized (memberUuid.toString().intern()) {
			if (repository.existsById(memberUuid.value())) {
				return;
			}
			try {
				repository.save(TokenWalletStateJpaEntity.create(memberUuid.value(), Instant.now()));
				repository.flush();
			} catch (DataIntegrityViolationException exception) {
				log.debug(
						"TokenWalletStateInitializer : ensureExists : Wallet 상태 동시 생성 감지 - memberUuid={}",
						memberUuid
				);
			}
		}
	}
}
