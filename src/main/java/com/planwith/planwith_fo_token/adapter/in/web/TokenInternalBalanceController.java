package com.planwith.planwith_fo_token.adapter.in.web;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_token.adapter.in.web.dto.TokenBalanceResponse;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.TokenBalanceResult;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

/**
 * 다른 MSA 서비스(Gateway/BFF 등)가 회원 UUID로 토큰 잔액을 조회할 때 사용하는 내부 API.
 * FO 화면 API({@link TokenBalanceQueryController})와 동일한 Query UseCase를 재사용한다.
 */
@RestController
@RequestMapping("/internal/planwith-fo-token/v1/members/{memberUuid}/tokens")
public class TokenInternalBalanceController {

	private static final Logger log = LoggerFactory.getLogger(TokenInternalBalanceController.class);

	private final GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase;

	public TokenInternalBalanceController(GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase) {
		this.getTokenBalanceQueryUseCase = getTokenBalanceQueryUseCase;
	}

	// 내부 토큰 잔액 조회
	@GetMapping("/balance")
	public TokenBalanceResponse getBalance(@PathVariable UUID memberUuid) {
		log.info("TokenInternalBalanceController : GET getBalance : 내부 토큰 잔액 조회 요청 - memberUuid={}",
				memberUuid);
		TokenBalanceResult result = getTokenBalanceQueryUseCase.getBalance(
				new GetTokenBalanceQuery(MemberUuid.from(memberUuid.toString()))
		);
		TokenBalanceResponse response = new TokenBalanceResponse(
				result.totalBalance(),
				result.paidBalance(),
				result.freeBalance(),
				result.bonusBalance(),
				result.memberUuid().value()
		);
		log.info(
				"TokenInternalBalanceController : GET getBalance : 내부 토큰 잔액 조회 완료 - memberUuid={}, totalBalance={}",
				memberUuid,
				response.totalBalance()
		);
		return response;
	}
}
