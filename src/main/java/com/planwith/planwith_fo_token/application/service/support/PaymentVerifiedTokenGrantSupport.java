package com.planwith.planwith_fo_token.application.service.support;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_token.application.command.ChargeTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.ChargeTokenUseCase;
import com.planwith.planwith_fo_token.application.port.out.TokenChargePort;
import com.planwith.planwith_fo_token.application.port.out.payment.PaymentInquiryResult;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;
import com.planwith.planwith_fo_token.domain.exception.ChargeAmountMismatchException;
import com.planwith.planwith_fo_token.domain.exception.PaymentVerificationException;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;
import com.planwith.planwith_fo_token.domain.model.TokenProduct;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;
import com.planwith.planwith_fo_token.domain.service.TokenProductPolicy;

/**
 * PG 재조회 결과를 검증한 뒤 PAID/FAILED/CANCELED 처리와 유료 토큰 지급을 수행한다.
 */
@Component
public class PaymentVerifiedTokenGrantSupport {

	private static final Logger log = LoggerFactory.getLogger(PaymentVerifiedTokenGrantSupport.class);

	private final TokenChargePort tokenChargePort;
	private final ChargeTokenUseCase chargeTokenUseCase;

	public PaymentVerifiedTokenGrantSupport(
			TokenChargePort tokenChargePort,
			ChargeTokenUseCase chargeTokenUseCase
	) {
		this.tokenChargePort = tokenChargePort;
		this.chargeTokenUseCase = chargeTokenUseCase;
	}

	public TokenChargeRequestResult verifyAndGrant(TokenCharge charge, PaymentInquiryResult inquiry) {
		log.info(
				"PaymentVerifiedTokenGrantSupport : verifyAndGrant : PG 결제 검증 시작 - chargeUuid={}, providerPaymentId={}, pgStatus={}, pgAmount={}",
				charge.chargeUuid(),
				inquiry.paymentId(),
				inquiry.status(),
				inquiry.totalAmount()
		);

		verifyProduct(charge);
		PaymentInquiryStatus inquiryStatus = PaymentInquiryStatus.from(inquiry.status());

		if (inquiryStatus == PaymentInquiryStatus.FAILED) {
			TokenCharge failed = tokenChargePort.save(charge.markFailed(inquiry.paymentId()));
			log.warn(
					"PaymentVerifiedTokenGrantSupport : verifyAndGrant : PG 결제 FAILED 처리 - chargeUuid={}",
					failed.chargeUuid()
			);
			return toResult(failed);
		}
		if (inquiryStatus == PaymentInquiryStatus.CANCELED) {
			TokenCharge canceled = tokenChargePort.save(charge.markCanceled(inquiry.paymentId()));
			log.warn(
					"PaymentVerifiedTokenGrantSupport : verifyAndGrant : PG 결제 CANCELED 처리 - chargeUuid={}",
					canceled.chargeUuid()
			);
			return toResult(canceled);
		}
		if (inquiryStatus != PaymentInquiryStatus.PAID) {
			TokenCharge failed = tokenChargePort.save(charge.markFailed(inquiry.paymentId()));
			log.warn(
					"PaymentVerifiedTokenGrantSupport : verifyAndGrant : PG 결제 상태 미확인으로 FAILED 처리 - chargeUuid={}, pgStatus={}",
					failed.chargeUuid(),
					inquiry.status()
			);
			return toResult(failed);
		}

		if (inquiry.totalAmount() != charge.paidAmount()) {
			TokenCharge failed = tokenChargePort.save(charge.markFailed(inquiry.paymentId()));
			log.warn(
					"PaymentVerifiedTokenGrantSupport : verifyAndGrant : PG 금액 불일치로 FAILED 처리 - chargeUuid={}, expected={}, actual={}",
					failed.chargeUuid(),
					charge.paidAmount(),
					inquiry.totalAmount()
			);
			return toResult(failed);
		}

		TransactionUuid ledgerTransactionUuid = new TransactionUuid(charge.chargeUuid().value());
		chargeTokenUseCase.charge(new ChargeTokenCommand(
				ledgerTransactionUuid,
				charge.memberUuid(),
				charge.tokenAmount(),
				"PAYMENT",
				charge.chargeUuid().toString(),
				"token charge " + (charge.productCode() == null ? "" : charge.productCode().name())
		));

		Instant paidAt = inquiry.paidAt() == null ? Instant.now() : inquiry.paidAt();
		TokenCharge paid = tokenChargePort.save(charge.markPaid(
				ledgerTransactionUuid,
				inquiry.paymentId(),
				paidAt
		));

		log.info(
				"PaymentVerifiedTokenGrantSupport : verifyAndGrant : 결제 검증 및 유료 토큰 지급 완료 - chargeUuid={}, providerPaymentId={}, tokenAmount={}",
				paid.chargeUuid(),
				paid.providerPaymentId(),
				paid.tokenAmount()
		);
		return toResult(paid);
	}

	public void verifyLocalAmount(TokenCharge charge, Long requestedPaidAmount) {
		verifyProduct(charge);
		if (requestedPaidAmount != null && requestedPaidAmount != charge.paidAmount()) {
			throw new ChargeAmountMismatchException(
					"Requested paidAmount does not match charge. expected="
							+ charge.paidAmount()
							+ ", actual="
							+ requestedPaidAmount
			);
		}
	}

	private void verifyProduct(TokenCharge charge) {
		if (charge.productCode() == null) {
			throw new PaymentVerificationException(
					"Charge product code is required for payment verification. chargeUuid="
							+ charge.chargeUuid().value()
			);
		}
		TokenProduct product = TokenProductPolicy.require(charge.productCode());
		if (product.salePrice() != charge.paidAmount()) {
			throw new ChargeAmountMismatchException(
					"Charge paidAmount does not match product policy. chargeUuid="
							+ charge.chargeUuid().value()
			);
		}
		if (product.totalTokenAmount() != charge.tokenAmount()) {
			throw new PaymentVerificationException(
					"Charge tokenAmount does not match product policy. chargeUuid="
							+ charge.chargeUuid().value()
							+ ", expected="
							+ product.totalTokenAmount()
							+ ", actual="
							+ charge.tokenAmount()
			);
		}
	}

	public static TokenChargeRequestResult toResult(TokenCharge charge) {
		return new TokenChargeRequestResult(
				charge.chargeUuid().value(),
				charge.productCode(),
				charge.status(),
				charge.tokenAmount(),
				charge.paidAmount(),
				charge.paymentMethodUuid() == null ? null : charge.paymentMethodUuid().value(),
				charge.paymentType(),
				charge.createdAt()
		);
	}
}
