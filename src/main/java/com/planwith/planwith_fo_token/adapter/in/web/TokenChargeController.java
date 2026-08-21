package com.planwith.planwith_fo_token.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_token.adapter.in.web.dto.ConfirmTokenChargeRequest;
import com.planwith.planwith_fo_token.adapter.in.web.dto.PayTokenChargeRequest;
import com.planwith.planwith_fo_token.adapter.in.web.dto.RequestTokenChargeRequest;
import com.planwith.planwith_fo_token.adapter.in.web.dto.TokenChargeResponse;
import com.planwith.planwith_fo_token.adapter.in.web.dto.TokenProductResponse;
import com.planwith.planwith_fo_token.application.command.ConfirmTokenChargeCommand;
import com.planwith.planwith_fo_token.application.command.PayTokenChargeCommand;
import com.planwith.planwith_fo_token.application.command.RequestTokenChargeCommand;
import com.planwith.planwith_fo_token.application.port.in.command.ConfirmTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.PayTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.RequestTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.ListTokenProductsQueryUseCase;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;
import com.planwith.planwith_fo_token.application.query.TokenProductResult;
import com.planwith.planwith_fo_token.domain.model.PaymentType;
import com.planwith.planwith_fo_token.domain.model.vo.ChargeUuid;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

@RestController
@RequestMapping("/api/planwith-fo-token")
public class TokenChargeController {

	private static final Logger log = LoggerFactory.getLogger(TokenChargeController.class);

	private final ListTokenProductsQueryUseCase listTokenProductsQueryUseCase;
	private final RequestTokenChargeUseCase requestTokenChargeUseCase;
	private final PayTokenChargeUseCase payTokenChargeUseCase;
	private final ConfirmTokenChargeUseCase confirmTokenChargeUseCase;

	public TokenChargeController(
			ListTokenProductsQueryUseCase listTokenProductsQueryUseCase,
			RequestTokenChargeUseCase requestTokenChargeUseCase,
			PayTokenChargeUseCase payTokenChargeUseCase,
			ConfirmTokenChargeUseCase confirmTokenChargeUseCase
	) {
		this.listTokenProductsQueryUseCase = listTokenProductsQueryUseCase;
		this.requestTokenChargeUseCase = requestTokenChargeUseCase;
		this.payTokenChargeUseCase = payTokenChargeUseCase;
		this.confirmTokenChargeUseCase = confirmTokenChargeUseCase;
	}

	// 토큰 상품 목록 조회
	@GetMapping("/members/{memberUuid}/token-products")
	public List<TokenProductResponse> listProducts(@PathVariable UUID memberUuid) {
		log.info("TokenChargeController : GET listProducts : 토큰 상품 목록 조회 요청 - memberUuid={}", memberUuid);
		List<TokenProductResponse> response = listTokenProductsQueryUseCase.listProducts().stream()
				.map(TokenChargeController::toProductResponse)
				.toList();
		log.info(
				"TokenChargeController : GET listProducts : 토큰 상품 목록 조회 완료 - memberUuid={}, count={}",
				memberUuid,
				response.size()
		);
		return response;
	}

	// 토큰 충전 요청 생성 (READY)
	@PostMapping("/members/{memberUuid}/charges")
	@ResponseStatus(HttpStatus.CREATED)
	public TokenChargeResponse requestCharge(
			@PathVariable UUID memberUuid,
			@RequestBody RequestTokenChargeRequest request
	) {
		log.info(
				"TokenChargeController : POST requestCharge : 토큰 충전 요청 생성 - memberUuid={}, productCode={}, paymentType={}",
				memberUuid,
				request.productCode(),
				request.paymentType()
		);
		TokenChargeResponse response = toChargeResponse(requestTokenChargeUseCase.request(
				toRequestCommand(memberUuid, request)
		));
		log.info(
				"TokenChargeController : POST requestCharge : 토큰 충전 READY 생성 완료 - memberUuid={}, chargeUuid={}",
				memberUuid,
				response.chargeUuid()
		);
		return response;
	}

	// 토큰 충전 결제 (ONE_TIME / BILLING_KEY)
	@PostMapping("/members/{memberUuid}/charges/{chargeUuid}/pay")
	public TokenChargeResponse payCharge(
			@PathVariable UUID memberUuid,
			@PathVariable UUID chargeUuid,
			@RequestBody(required = false) PayTokenChargeRequest request
	) {
		log.info(
				"TokenChargeController : POST payCharge : 토큰 충전 결제 요청 - memberUuid={}, chargeUuid={}",
				memberUuid,
				chargeUuid
		);
		TokenChargeResponse response = toChargeResponse(payTokenChargeUseCase.pay(new PayTokenChargeCommand(
				MemberUuid.from(memberUuid.toString()),
				new ChargeUuid(chargeUuid),
				request == null ? null : request.paidAmount()
		)));
		log.info(
				"TokenChargeController : POST payCharge : 토큰 충전 결제 완료 - memberUuid={}, chargeUuid={}, status={}",
				memberUuid,
				chargeUuid,
				response.status()
		);
		return response;
	}

	// 결제 검증 후 유료 토큰 지급
	@PostMapping("/members/{memberUuid}/charges/{chargeUuid}/confirm")
	public TokenChargeResponse confirmCharge(
			@PathVariable UUID memberUuid,
			@PathVariable UUID chargeUuid,
			@RequestBody ConfirmTokenChargeRequest request
	) {
		log.info(
				"TokenChargeController : POST confirmCharge : 결제 검증 및 토큰 지급 요청 - memberUuid={}, chargeUuid={}",
				memberUuid,
				chargeUuid
		);
		TokenChargeResponse response = toChargeResponse(confirmTokenChargeUseCase.confirm(new ConfirmTokenChargeCommand(
				MemberUuid.from(memberUuid.toString()),
				new ChargeUuid(chargeUuid),
				request == null ? null : request.providerPaymentId(),
				request == null ? null : request.paidAmount()
		)));
		log.info(
				"TokenChargeController : POST confirmCharge : 결제 검증 및 토큰 지급 완료 - memberUuid={}, chargeUuid={}, status={}",
				memberUuid,
				chargeUuid,
				response.status()
		);
		return response;
	}

	private static RequestTokenChargeCommand toRequestCommand(UUID memberUuid, RequestTokenChargeRequest request) {
		if (request.productCode() == null || request.productCode().isBlank()) {
			throw new IllegalArgumentException("productCode is required.");
		}
		PaymentType paymentType = PaymentType.BILLING_KEY;
		if (request.paymentType() != null && !request.paymentType().isBlank()) {
			try {
				paymentType = PaymentType.valueOf(request.paymentType().trim().toUpperCase());
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException("Invalid paymentType. value=" + request.paymentType());
			}
		}
		if (paymentType == PaymentType.BILLING_KEY && request.paymentMethodUuid() == null) {
			throw new IllegalArgumentException("paymentMethodUuid is required for BILLING_KEY payment.");
		}
		return new RequestTokenChargeCommand(
				MemberUuid.from(memberUuid.toString()),
				request.productCode(),
				request.paymentMethodUuid() == null ? null : new PaymentMethodUuid(request.paymentMethodUuid()),
				paymentType,
				request.clientRequestId()
		);
	}

	private static TokenProductResponse toProductResponse(TokenProductResult result) {
		return new TokenProductResponse(
				result.code().name(),
				result.name(),
				result.salePrice(),
				result.baseTokenAmount(),
				result.bonusTokenAmount(),
				result.totalTokenAmount()
		);
	}

	private static TokenChargeResponse toChargeResponse(TokenChargeRequestResult result) {
		return new TokenChargeResponse(
				result.chargeUuid(),
				result.productCode() == null ? null : result.productCode().name(),
				result.status().name(),
				result.tokenAmount(),
				result.paidAmount(),
				result.paymentMethodUuid(),
				result.paymentType() == null ? null : result.paymentType().name(),
				result.createdAt()
		);
	}
}
