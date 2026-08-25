package com.planwith.planwith_fo_token.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_token.adapter.in.web.dto.PaymentMethodResponse;
import com.planwith.planwith_fo_token.adapter.in.web.dto.RegisterPaymentMethodRequest;
import com.planwith.planwith_fo_token.application.command.DeletePaymentMethodCommand;
import com.planwith.planwith_fo_token.application.command.RegisterPaymentMethodCommand;
import com.planwith.planwith_fo_token.application.command.SetDefaultPaymentMethodCommand;
import com.planwith.planwith_fo_token.application.port.in.command.DeletePaymentMethodUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.RegisterPaymentMethodUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.SetDefaultPaymentMethodUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.ListPaymentMethodsQueryUseCase;
import com.planwith.planwith_fo_token.application.query.ListPaymentMethodsQuery;
import com.planwith.planwith_fo_token.application.query.PaymentMethodResult;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

@RestController
@RequestMapping("/api/planwith-fo-token/members/{memberUuid}/payment-methods")
public class PaymentMethodController {

	private static final Logger log = LoggerFactory.getLogger(PaymentMethodController.class);

	private final RegisterPaymentMethodUseCase registerPaymentMethodUseCase;
	private final ListPaymentMethodsQueryUseCase listPaymentMethodsQueryUseCase;
	private final SetDefaultPaymentMethodUseCase setDefaultPaymentMethodUseCase;
	private final DeletePaymentMethodUseCase deletePaymentMethodUseCase;

	public PaymentMethodController(
			RegisterPaymentMethodUseCase registerPaymentMethodUseCase,
			ListPaymentMethodsQueryUseCase listPaymentMethodsQueryUseCase,
			SetDefaultPaymentMethodUseCase setDefaultPaymentMethodUseCase,
			DeletePaymentMethodUseCase deletePaymentMethodUseCase
	) {
		this.registerPaymentMethodUseCase = registerPaymentMethodUseCase;
		this.listPaymentMethodsQueryUseCase = listPaymentMethodsQueryUseCase;
		this.setDefaultPaymentMethodUseCase = setDefaultPaymentMethodUseCase;
		this.deletePaymentMethodUseCase = deletePaymentMethodUseCase;
	}

	// 카드 등록
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PaymentMethodResponse register(
			@PathVariable UUID memberUuid,
			@RequestBody RegisterPaymentMethodRequest request
	) {
		log.info("PaymentMethodController : POST register : 카드 등록 요청 - memberUuid={}", memberUuid);
		PaymentMethodResponse response = toResponse(registerPaymentMethodUseCase.register(
				toRegisterCommand(memberUuid, request)
		));
		log.info(
				"PaymentMethodController : POST register : 카드 등록 완료 - memberUuid={}, paymentMethodUuid={}",
				memberUuid,
				response.paymentMethodUuid()
		);
		return response;
	}

	// 카드 목록 조회 (ACTIVE)
	@GetMapping
	public List<PaymentMethodResponse> list(@PathVariable UUID memberUuid) {
		log.info("PaymentMethodController : GET list : 카드 목록 조회 요청 - memberUuid={}", memberUuid);
		List<PaymentMethodResponse> response = listPaymentMethodsQueryUseCase.list(
						new ListPaymentMethodsQuery(MemberUuid.from(memberUuid.toString()))
				).stream()
				.map(PaymentMethodController::toResponse)
				.toList();
		log.info(
				"PaymentMethodController : GET list : 카드 목록 조회 완료 - memberUuid={}, count={}",
				memberUuid,
				response.size()
		);
		return response;
	}

	// 기본 결제수단 변경
	@PostMapping("/{paymentMethodUuid}/default")
	public PaymentMethodResponse setDefault(
			@PathVariable UUID memberUuid,
			@PathVariable UUID paymentMethodUuid
	) {
		log.info(
				"PaymentMethodController : POST setDefault : 기본 결제수단 변경 요청 - memberUuid={}, paymentMethodUuid={}",
				memberUuid,
				paymentMethodUuid
		);
		PaymentMethodResponse response = toResponse(setDefaultPaymentMethodUseCase.setDefault(
				new SetDefaultPaymentMethodCommand(
						MemberUuid.from(memberUuid.toString()),
						new PaymentMethodUuid(paymentMethodUuid)
				)
		));
		log.info(
				"PaymentMethodController : POST setDefault : 기본 결제수단 변경 완료 - memberUuid={}, paymentMethodUuid={}",
				memberUuid,
				paymentMethodUuid
		);
		return response;
	}

	// 카드 삭제 (soft delete: ACTIVE → DELETED)
	@DeleteMapping("/{paymentMethodUuid}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(
			@PathVariable UUID memberUuid,
			@PathVariable UUID paymentMethodUuid
	) {
		log.info(
				"PaymentMethodController : DELETE delete : 카드 삭제 요청 - memberUuid={}, paymentMethodUuid={}",
				memberUuid,
				paymentMethodUuid
		);
		deletePaymentMethodUseCase.delete(new DeletePaymentMethodCommand(
				MemberUuid.from(memberUuid.toString()),
				new PaymentMethodUuid(paymentMethodUuid)
		));
		log.info(
				"PaymentMethodController : DELETE delete : 카드 삭제 완료 - memberUuid={}, paymentMethodUuid={}",
				memberUuid,
				paymentMethodUuid
		);
	}

	private static RegisterPaymentMethodCommand toRegisterCommand(
			UUID memberUuid,
			RegisterPaymentMethodRequest request
	) {
		if (request.cardName() == null || request.cardName().isBlank()) {
			throw new IllegalArgumentException("cardName is required.");
		}
		if (request.cardNumber() == null || request.cardNumber().isBlank()) {
			throw new IllegalArgumentException("cardNumber is required.");
		}
		if (request.expiryYear() == null || request.expiryYear().isBlank()) {
			throw new IllegalArgumentException("expiryYear is required.");
		}
		if (request.expiryMonth() == null || request.expiryMonth().isBlank()) {
			throw new IllegalArgumentException("expiryMonth is required.");
		}
		if (request.birthOrBusinessRegistrationNumber() == null
				|| request.birthOrBusinessRegistrationNumber().isBlank()) {
			throw new IllegalArgumentException("birthOrBusinessRegistrationNumber is required.");
		}
		if (request.passwordTwoDigits() == null || request.passwordTwoDigits().isBlank()) {
			throw new IllegalArgumentException("passwordTwoDigits is required.");
		}
		return new RegisterPaymentMethodCommand(
				MemberUuid.from(memberUuid.toString()),
				request.cardName(),
				request.cardNumber(),
				request.expiryYear(),
				request.expiryMonth(),
				request.birthOrBusinessRegistrationNumber(),
				request.passwordTwoDigits(),
				Boolean.TRUE.equals(request.defaultMethod())
		);
	}

	private static PaymentMethodResponse toResponse(PaymentMethodResult result) {
		return new PaymentMethodResponse(
				result.paymentMethodUuid(),
				result.cardName(),
				result.fourCardNumber(),
				result.defaultMethod(),
				result.registeredAt()
		);
	}
}
