package com.planwith.planwith_fo_token.adapter.out.persistence.gradereward;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.GradeMonthlyTokenGrantPort;
import com.planwith.planwith_fo_token.domain.model.GradeMonthlyTokenGrant;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@Component
public class GradeMonthlyTokenGrantPersistenceAdapter implements GradeMonthlyTokenGrantPort {

	private final SpringDataGradeMonthlyTokenGrantRepository repository;

	public GradeMonthlyTokenGrantPersistenceAdapter(SpringDataGradeMonthlyTokenGrantRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByMemberUuidAndRewardMonth(MemberUuid memberUuid, String rewardMonth) {
		return repository.existsByMemberUuidAndRewardMonth(
				memberUuid.value(),
				GradeMonthlyTokenGrant.requireRewardMonth(rewardMonth)
		);
	}

	@Override
	@Transactional
	public boolean saveIdempotent(GradeMonthlyTokenGrant grant) {
		if (existsByMemberUuidAndRewardMonth(grant.memberUuid(), grant.rewardMonth())) {
			return false;
		}
		try {
			repository.save(GradeMonthlyTokenGrantJpaEntity.create(
					grant.memberUuid().value(),
					grant.rewardMonth(),
					grant.eventUuid(),
					grant.ledgerTransactionUuid().value(),
					grant.tokenAmount(),
					grant.gradeCode(),
					grant.grantedAt()
			));
			return true;
		} catch (DataIntegrityViolationException exception) {
			return false;
		}
	}
}
