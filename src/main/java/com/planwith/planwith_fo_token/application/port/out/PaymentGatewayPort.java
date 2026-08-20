package com.planwith.planwith_fo_token.application.port.out;

import com.planwith.planwith_fo_token.application.port.out.payment.CancelPaymentRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.CancelPaymentResult;
import com.planwith.planwith_fo_token.application.port.out.payment.IssueBillingKeyRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.IssueBillingKeyResult;
import com.planwith.planwith_fo_token.application.port.out.payment.PayRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.PayResult;
import com.planwith.planwith_fo_token.application.port.out.payment.PayWithBillingKeyRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.PaymentInquiryResult;

public interface PaymentGatewayPort {

	IssueBillingKeyResult issueBillingKey(IssueBillingKeyRequest request);

	PayResult pay(PayRequest request);

	PayResult payWithBillingKey(PayWithBillingKeyRequest request);

	PaymentInquiryResult getPayment(String paymentId);

	CancelPaymentResult cancelPayment(CancelPaymentRequest request);
}
