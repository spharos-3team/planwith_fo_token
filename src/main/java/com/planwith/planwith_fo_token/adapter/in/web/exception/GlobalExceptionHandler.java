package com.planwith.planwith_fo_token.adapter.in.web.exception;

import java.time.Instant;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.planwith.planwith_fo_token.adapter.in.web.dto.ApiErrorResponse;

import com.planwith.planwith_fo_token.application.exception.PaymentGatewayException;
import com.planwith.planwith_fo_token.domain.exception.DuplicateTransactionException;
import com.planwith.planwith_fo_token.domain.exception.InsufficientTokenBalanceException;
import com.planwith.planwith_fo_token.domain.exception.InvalidPaymentMethodStateException;
import com.planwith.planwith_fo_token.domain.exception.PaymentMethodNotFoundException;

import jakarta.persistence.OptimisticLockException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InsufficientTokenBalanceException.class)
	public ResponseEntity<ApiErrorResponse> handleInsufficientBalance(InsufficientTokenBalanceException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, "TOKEN_INSUFFICIENT", exception.getMessage());
	}

	@ExceptionHandler(DuplicateTransactionException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicateTransaction(DuplicateTransactionException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_IDEMPOTENCY_KEY", exception.getMessage());
	}

	@ExceptionHandler(OptimisticLockException.class)
	public ResponseEntity<ApiErrorResponse> handleOptimisticLock(OptimisticLockException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, "WALLET_VERSION_CONFLICT", exception.getMessage());
	}

	@ExceptionHandler(InvalidPaymentMethodStateException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidPaymentMethodState(
			InvalidPaymentMethodStateException exception
	) {
		return createErrorResponse(HttpStatus.CONFLICT, "INVALID_PAYMENT_METHOD_STATE", exception.getMessage());
	}

	@ExceptionHandler(PaymentMethodNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handlePaymentMethodNotFound(PaymentMethodNotFoundException exception) {
		return createErrorResponse(HttpStatus.NOT_FOUND, "PAYMENT_METHOD_NOT_FOUND", exception.getMessage());
	}

	@ExceptionHandler(PaymentGatewayException.class)
	public ResponseEntity<ApiErrorResponse> handlePaymentGateway(PaymentGatewayException exception) {
		return createErrorResponse(HttpStatus.BAD_GATEWAY, "PAYMENT_GATEWAY_ERROR", exception.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage());
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
		return createErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.findFirst()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.orElse("요청값이 올바르지 않습니다.");

		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
	}

	private ResponseEntity<ApiErrorResponse> createErrorResponse(
			HttpStatus status,
			String code,
			String message
	) {
		ApiErrorResponse response = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				code,
				message
		);
		return ResponseEntity.status(status).body(response);
	}
}
