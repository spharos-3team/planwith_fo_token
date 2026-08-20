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

import com.planwith.planwith_fo_token.adapter.in.web.dto.TokenChargeHistoryResponse;
import com.planwith.planwith_fo_token.adapter.in.web.dto.TokenLedgerEntryResponse;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenChargeHistoryQueryUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenLedgerQueryUseCase;
import com.planwith.planwith_fo_token.application.query.GetTokenChargeHistoryQuery;
import com.planwith.planwith_fo_token.application.query.GetTokenLedgerQuery;
import com.planwith.planwith_fo_token.application.query.TokenChargeHistoryResult;
import com.planwith.planwith_fo_token.application.query.TokenLedgerEntryResult;
import com.planwith.planwith_fo_token.domain.model.TransactionType;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@RestController
@RequestMapping("/api/planwith-fo-token/members/{memberUuid}/tokens")
public class TokenHistoryQueryController {

	private static final Logger log = LoggerFactory.getLogger(TokenHistoryQueryController.class);

	private final GetTokenLedgerQueryUseCase getTokenLedgerQueryUseCase;
	private final GetTokenChargeHistoryQueryUseCase getTokenChargeHistoryQueryUseCase;

	public TokenHistoryQueryController(
			GetTokenLedgerQueryUseCase getTokenLedgerQueryUseCase,
			GetTokenChargeHistoryQueryUseCase getTokenChargeHistoryQueryUseCase
	) {
		this.getTokenLedgerQueryUseCase = getTokenLedgerQueryUseCase;
		this.getTokenChargeHistoryQueryUseCase = getTokenChargeHistoryQueryUseCase;
	}

	// 토큰 거래 내역 조회 (CHARGE / USE / REWARD / EXPIRE)
	@GetMapping("/ledger")
	public List<TokenLedgerEntryResponse> getLedger(
			@PathVariable UUID memberUuid,
			@RequestParam(required = false) TransactionType type,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		log.info(
				"TokenHistoryQueryController : GET getLedger : 토큰 거래 내역 조회 요청 - memberUuid={}, type={}",
				memberUuid,
				type
		);
		List<TokenLedgerEntryResponse> response = getTokenLedgerQueryUseCase.getLedger(
						new GetTokenLedgerQuery(MemberUuid.from(memberUuid.toString()), type, page, size)
				)
				.stream()
				.map(TokenHistoryQueryController::toLedgerResponse)
				.toList();
		log.info(
				"TokenHistoryQueryController : GET getLedger : 토큰 거래 내역 조회 완료 - memberUuid={}, count={}",
				memberUuid,
				response.size()
		);
		return response;
	}

	// 토큰 충전 내역 조회 (token_charge 기반, PNG 토큰 내역 탭)
	@GetMapping("/charges")
	public List<TokenChargeHistoryResponse> getChargeHistory(
			@PathVariable UUID memberUuid,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		log.info("TokenHistoryQueryController : GET getChargeHistory : 토큰 충전 내역 조회 요청 - memberUuid={}",
				memberUuid);
		List<TokenChargeHistoryResponse> response = getTokenChargeHistoryQueryUseCase.getChargeHistory(
						new GetTokenChargeHistoryQuery(MemberUuid.from(memberUuid.toString()), page, size)
				)
				.stream()
				.map(TokenHistoryQueryController::toChargeResponse)
				.toList();
		log.info(
				"TokenHistoryQueryController : GET getChargeHistory : 토큰 충전 내역 조회 완료 - memberUuid={}, count={}",
				memberUuid,
				response.size()
		);
		return response;
	}

	private static TokenLedgerEntryResponse toLedgerResponse(TokenLedgerEntryResult entry) {
		return new TokenLedgerEntryResponse(
				entry.occurredAt(),
				entry.transactionType(),
				entry.amountChange(),
				entry.amount(),
				entry.balanceAfter(),
				entry.usagePlace(),
				entry.description(),
				entry.tokenType(),
				entry.transactionUuid().value(),
				entry.ledgerId()
		);
	}

	private static TokenChargeHistoryResponse toChargeResponse(TokenChargeHistoryResult entry) {
		return new TokenChargeHistoryResponse(
				entry.paymentCode(),
				entry.chargedAt(),
				entry.tokenAmount(),
				entry.paidAmount(),
				entry.paymentMethodName(),
				entry.cardLastFour(),
				entry.status(),
				entry.paymentType(),
				entry.chargeUuid()
		);
	}
}
