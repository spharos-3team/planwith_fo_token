package com.planwith.planwith_fo_token.adapter.out.persistence.gradereward;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataGradeMonthlyTokenGrantRepository extends JpaRepository<GradeMonthlyTokenGrantJpaEntity, Long> {

	boolean existsByMemberUuidAndRewardMonth(UUID memberUuid, String rewardMonth);
}
