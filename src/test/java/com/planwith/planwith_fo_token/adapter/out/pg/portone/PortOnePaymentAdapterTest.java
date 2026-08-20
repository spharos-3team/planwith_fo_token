package com.planwith.planwith_fo_token.adapter.out.pg.portone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_token.application.exception.PaymentGatewayException;
import com.planwith.planwith_fo_token.application.port.out.PaymentGatewayPort;
import com.planwith.planwith_fo_token.application.port.out.payment.CancelPaymentRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.CardCredential;
import com.planwith.planwith_fo_token.application.port.out.payment.IssueBillingKeyRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.IssueBillingKeyResult;
import com.planwith.planwith_fo_token.application.port.out.payment.PayRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.PayResult;
import com.planwith.planwith_fo_token.application.port.out.payment.PayWithBillingKeyRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.PaymentInquiryResult;
import com.planwith.planwith_fo_token.config.PortOneProperties;

@SpringBootTest
@ActiveProfiles("test")
class PortOnePaymentAdapterTest {

	@Autowired
	private PaymentGatewayPort paymentGatewayPort;

	@Autowired
	private PortOneProperties portOneProperties;

	@Test
	void stubModeIssuesBillingKeyWithoutExternalApi() {
		IssueBillingKeyResult result = paymentGatewayPort.issueBillingKey(new IssueBillingKeyRequest(
				"member-1",
				null,
				new CardCredential("4111111111111111", "28", "12", "900101", "12")
		));

		assertThat(portOneProperties.isStubEnabled()).isTrue();
		assertThat(result.billingKey()).startsWith("stub-billing-key-");
		assertThat(result.fourCardNumber()).isEqualTo("1111");
	}

	@Test
	void stubModeSupportsPaymentLifecycle() {
		String paymentId = "payment-123";

		PayResult payResult = paymentGatewayPort.pay(new PayRequest(
				paymentId, "토큰 충전", 10000L, "KRW", null
		));
		PayResult billingPayResult = paymentGatewayPort.payWithBillingKey(new PayWithBillingKeyRequest(
				paymentId + "-billing", "stub-billing-key", "토큰 충전", 10000L, "KRW", null
		));
		PaymentInquiryResult inquiryResult = paymentGatewayPort.getPayment(paymentId);
		var cancelResult = paymentGatewayPort.cancelPayment(new CancelPaymentRequest(paymentId, "test", null));

		assertThat(payResult.paymentId()).isEqualTo(paymentId);
		assertThat(payResult.status()).isEqualTo("PAID");
		assertThat(billingPayResult.status()).isEqualTo("PAID");
		assertThat(inquiryResult.paymentId()).isEqualTo(paymentId);
		assertThat(cancelResult.status()).isEqualTo("CANCELLED");
	}

	@Test
	void liveModeRequiresConfiguration() {
		PortOneProperties properties = new PortOneProperties();
		properties.setStubEnabled(false);
		PortOnePaymentAdapter adapter = new PortOnePaymentAdapter(properties, new ObjectMapper());

		assertThatThrownBy(() -> adapter.issueBillingKey(new IssueBillingKeyRequest(
				"member-1",
				null,
				new CardCredential("4111111111111111", "28", "12", "900101", "12")
		))).isInstanceOf(PaymentGatewayException.class)
				.hasMessageContaining("not configured");
	}
}
