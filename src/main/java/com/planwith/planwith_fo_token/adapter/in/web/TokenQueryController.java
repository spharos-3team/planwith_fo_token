package com.planwith.planwith_fo_token.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_token.adapter.in.web.dto.TokenLedgerEntryResponse;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenChargeHistoryQueryUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenLedgerQueryUseCase;
import com.planwith.planwith_fo_token.application.query.GetTokenChargeHistoryQuery;
import com.planwith.planwith_fo_token.application.query.GetTokenLedgerQuery;
import com.planwith.planwith_fo_token.application.query.TokenLedgerEntryResult;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@RestController
@RequestMapping("/api/planwith-fo-token/members/{memberUuid}/tokens")
public class TokenQueryController {

	private static final Logger log = LoggerFactory.getLogger(TokenQueryController.class);

	private final GetTokenLedgerQueryUseCase getTokenLedgerQueryUseCase;
	private final GetTokenChargeHistoryQueryUseCase getTokenChargeHistoryQueryUseCase;

	public TokenQueryController(
			GetTokenLedgerQueryUseCase getTokenLedgerQueryUseCase,
			GetTokenChargeHistoryQueryUseCase getTokenChargeHistoryQueryUseCase
	) {
		this.getTokenLedgerQueryUseCase = getTokenLedgerQueryUseCase;
		this.getTokenChargeHistoryQueryUseCase = getTokenChargeHistoryQueryUseCase;
	}

	// 토큰 거래 내역 조회
	@GetMapping("/ledger")
	public List<TokenLedgerEntryResponse> getLedger(
			@PathVariable UUID memberUuid,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		log.info("TokenQueryController : GET getLedger : 토큰 거래 내역 조회 요청 - memberUuid={}", memberUuid);
		List<TokenLedgerEntryResponse> response = getTokenLedgerQueryUseCase.getLedger(
						new GetTokenLedgerQuery(MemberUuid.from(memberUuid.toString()), page, size)
				)
				.stream()
				.map(TokenQueryController::toLedgerResponse)
				.toList();
		log.info("TokenQueryController : GET getLedger : 토큰 거래 내역 조회 완료 - memberUuid={}, count={}",
				memberUuid, response.size());
		return response;
	}

	// 토큰 충전 내역 조회
	@GetMapping("/charges")
	public List<TokenLedgerEntryResponse> getChargeHistory(
			@PathVariable UUID memberUuid,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		log.info("TokenQueryController : GET getChargeHistory : 토큰 충전 내역 조회 요청 - memberUuid={}", memberUuid);
		List<TokenLedgerEntryResponse> response = getTokenChargeHistoryQueryUseCase.getChargeHistory(
						new GetTokenChargeHistoryQuery(MemberUuid.from(memberUuid.toString()), page, size)
				)
				.stream()
				.map(TokenQueryController::toLedgerResponse)
				.toList();
		log.info("TokenQueryController : GET getChargeHistory : 토큰 충전 내역 조회 완료 - memberUuid={}, count={}",
				memberUuid, response.size());
		return response;
	}

	private static TokenLedgerEntryResponse toLedgerResponse(TokenLedgerEntryResult entry) {
		return new TokenLedgerEntryResponse(
				entry.ledgerId(),
				entry.transactionUuid().value(),
				entry.memberUuid().value(),
				entry.entryType(),
				entry.tokenType(),
				entry.amount(),
				entry.balanceAfter(),
				entry.referenceType(),
				entry.description(),
				entry.occurredAt()
		);
	}
}
