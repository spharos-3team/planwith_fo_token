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
import com.planwith.planwith_fo_token.domain.exception.ChargeAmountMismatchException;
import com.planwith.planwith_fo_token.domain.exception.DuplicateTransactionException;
import com.planwith.planwith_fo_token.domain.exception.InsufficientTokenBalanceException;
import com.planwith.planwith_fo_token.domain.exception.InvalidChargeStateException;
import com.planwith.planwith_fo_token.domain.exception.InvalidPaymentMethodStateException;
import com.planwith.planwith_fo_token.domain.exception.PaymentMethodNotFoundException;
import com.planwith.planwith_fo_token.domain.exception.PaymentVerificationException;
import com.planwith.planwith_fo_token.domain.exception.TokenChargeNotFoundException;
import com.planwith.planwith_fo_token.domain.exception.TokenProductNotFoundException;
import com.planwith.planwith_fo_token.domain.exception.TokenWalletNotFoundException;
import com.planwith.planwith_fo_token.domain.exception.WalletLedgerInconsistencyException;

import jakarta.persistence.OptimisticLockException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InsufficientTokenBalanceException.class)
	public ResponseEntity<ApiErrorResponse> handleInsufficientBalance(InsufficientTokenBalanceException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, "TOKEN_INSUFFICIENT", exception.getMessage());
	}

	@ExceptionHandler(TokenWalletNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleTokenWalletNotFound(TokenWalletNotFoundException exception) {
		return createErrorResponse(HttpStatus.NOT_FOUND, "TOKEN_WALLET_NOT_FOUND", exception.getMessage());
	}

	@ExceptionHandler(DuplicateTransactionException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicateTransaction(DuplicateTransactionException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_TOKEN_TRANSACTION", exception.getMessage());
	}

	@ExceptionHandler(OptimisticLockException.class)
	public ResponseEntity<ApiErrorResponse> handleOptimisticLock(OptimisticLockException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, "WALLET_VERSION_CONFLICT", exception.getMessage());
	}

	@ExceptionHandler(InvalidPaymentMethodStateException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidPaymentMethodState(
			InvalidPaymentMethodStateException exception
	) {
		return createErrorResponse(HttpStatus.CONFLICT, "PAYMENT_METHOD_NOT_ACTIVE", exception.getMessage());
	}

	@ExceptionHandler(PaymentMethodNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handlePaymentMethodNotFound(PaymentMethodNotFoundException exception) {
		return createErrorResponse(HttpStatus.NOT_FOUND, "PAYMENT_METHOD_NOT_FOUND", exception.getMessage());
	}

	@ExceptionHandler(TokenProductNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleTokenProductNotFound(TokenProductNotFoundException exception) {
		return createErrorResponse(HttpStatus.NOT_FOUND, "TOKEN_PRODUCT_NOT_FOUND", exception.getMessage());
	}

	@ExceptionHandler(TokenChargeNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleTokenChargeNotFound(TokenChargeNotFoundException exception) {
		return createErrorResponse(HttpStatus.NOT_FOUND, "TOKEN_CHARGE_NOT_FOUND", exception.getMessage());
	}

	@ExceptionHandler(ChargeAmountMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleChargeAmountMismatch(ChargeAmountMismatchException exception) {
		return createErrorResponse(HttpStatus.BAD_REQUEST, "PAYMENT_AMOUNT_MISMATCH", exception.getMessage());
	}

	@ExceptionHandler(PaymentVerificationException.class)
	public ResponseEntity<ApiErrorResponse> handlePaymentVerification(PaymentVerificationException exception) {
		return createErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "PAYMENT_FAILED", exception.getMessage());
	}

	@ExceptionHandler(PaymentGatewayException.class)
	public ResponseEntity<ApiErrorResponse> handlePaymentGateway(PaymentGatewayException exception) {
		return createErrorResponse(HttpStatus.BAD_GATEWAY, "PAYMENT_FAILED", exception.getMessage());
	}

	@ExceptionHandler(InvalidChargeStateException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidChargeState(InvalidChargeStateException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, "INVALID_CHARGE_STATE", exception.getMessage());
	}

	@ExceptionHandler(WalletLedgerInconsistencyException.class)
	public ResponseEntity<ApiErrorResponse> handleWalletLedgerInconsistency(
			WalletLedgerInconsistencyException exception
	) {
		return createErrorResponse(HttpStatus.CONFLICT, "WALLET_LEDGER_INCONSISTENT", exception.getMessage());
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
