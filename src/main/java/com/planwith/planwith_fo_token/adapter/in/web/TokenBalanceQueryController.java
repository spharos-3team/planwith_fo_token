package com.planwith.planwith_fo_token.adapter.in.web;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_token.adapter.in.web.dto.TokenBalanceResponse;
import com.planwith.planwith_fo_token.adapter.in.web.dto.TokenBalanceSummaryResponse;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.TokenBalanceResult;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@RestController
@RequestMapping("/api/planwith-fo-token/members/{memberUuid}/tokens")
public class TokenBalanceQueryController {

	private static final Logger log = LoggerFactory.getLogger(TokenBalanceQueryController.class);

	private final GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase;

	public TokenBalanceQueryController(GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase) {
		this.getTokenBalanceQueryUseCase = getTokenBalanceQueryUseCase;
	}

	// 토큰 잔액 조회 (관리 화면)
	@GetMapping("/balance")
	public TokenBalanceResponse getBalance(@PathVariable UUID memberUuid) {
		log.info("TokenBalanceQueryController : GET getBalance : 토큰 잔액 조회 요청 - memberUuid={}", memberUuid);
		TokenBalanceResponse response = toBalanceResponse(getBalanceResult(memberUuid));
		log.info(
				"TokenBalanceQueryController : GET getBalance : 토큰 잔액 조회 완료 - memberUuid={}, totalBalance={}",
				memberUuid,
				response.totalBalance()
		);
		return response;
	}

	// Header 토큰 표시용 총 잔액 조회
	@GetMapping("/balance/summary")
	public TokenBalanceSummaryResponse getBalanceSummary(@PathVariable UUID memberUuid) {
		log.info("TokenBalanceQueryController : GET getBalanceSummary : Header 잔액 조회 요청 - memberUuid={}",
				memberUuid);
		long totalBalance = getBalanceResult(memberUuid).totalBalance();
		log.info(
				"TokenBalanceQueryController : GET getBalanceSummary : Header 잔액 조회 완료 - memberUuid={}, totalBalance={}",
				memberUuid,
				totalBalance
		);
		return new TokenBalanceSummaryResponse(totalBalance);
	}

	private TokenBalanceResult getBalanceResult(UUID memberUuid) {
		return getTokenBalanceQueryUseCase.getBalance(
				new GetTokenBalanceQuery(MemberUuid.from(memberUuid.toString()))
		);
	}

	private static TokenBalanceResponse toBalanceResponse(TokenBalanceResult result) {
		return new TokenBalanceResponse(
				result.totalBalance(),
				result.paidBalance(),
				result.freeBalance(),
				result.bonusBalance(),
				result.memberUuid().value()
		);
	}
}
