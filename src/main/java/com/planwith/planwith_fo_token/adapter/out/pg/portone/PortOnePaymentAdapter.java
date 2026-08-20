package com.planwith.planwith_fo_token.adapter.out.pg.portone;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_token.application.exception.PaymentGatewayException;
import com.planwith.planwith_fo_token.application.port.out.PaymentGatewayPort;
import com.planwith.planwith_fo_token.application.port.out.payment.CancelPaymentRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.CancelPaymentResult;
import com.planwith.planwith_fo_token.application.port.out.payment.IssueBillingKeyRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.IssueBillingKeyResult;
import com.planwith.planwith_fo_token.application.port.out.payment.PayRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.PayResult;
import com.planwith.planwith_fo_token.application.port.out.payment.PayWithBillingKeyRequest;
import com.planwith.planwith_fo_token.application.port.out.payment.PaymentInquiryResult;
import com.planwith.planwith_fo_token.config.PortOneProperties;

@Component
public class PortOnePaymentAdapter implements PaymentGatewayPort {

	private final PortOneProperties properties;
	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	public PortOnePaymentAdapter(PortOneProperties properties, ObjectMapper objectMapper) {
		this.properties = properties;
		this.objectMapper = objectMapper;
		this.restClient = RestClient.builder()
				.baseUrl(properties.getApiBaseUrl())
				.build();
	}

	@Override
	public IssueBillingKeyResult issueBillingKey(IssueBillingKeyRequest request) {
		if (properties.isStubEnabled()) {
			return stubIssueBillingKey(request);
		}
		validateLiveConfiguration();

		String channelKey = resolveChannelKey(request.channelKey());
		Map<String, Object> body = Map.of(
				"storeId", properties.getStoreId(),
				"channelKey", channelKey,
				"customer", Map.of("id", request.customerId()),
				"method", Map.of(
						"card", Map.of(
								"credential", Map.of(
										"number", request.cardCredential().number(),
										"expiryYear", request.cardCredential().expiryYear(),
										"expiryMonth", request.cardCredential().expiryMonth(),
										"birthOrBusinessRegistrationNumber",
										request.cardCredential().birthOrBusinessRegistrationNumber(),
										"passwordTwoDigits", request.cardCredential().passwordTwoDigits()
								)
						)
				)
		);

		JsonNode response = post("/billing-keys", body);
		JsonNode billingKeyInfo = response.path("billingKeyInfo");
		String billingKey = billingKeyInfo.path("billingKey").asText(null);
		if (billingKey == null || billingKey.isBlank()) {
			throw new PaymentGatewayException("PortOne billing key issue response is missing billingKey.");
		}
		JsonNode method = billingKeyInfo.path("method").path("card");
		String cardNumber = method.path("number").asText("").replaceAll("\\D", "");
		String fourCardNumber = cardNumber.length() >= 4
				? cardNumber.substring(cardNumber.length() - 4)
				: "";
		return new IssueBillingKeyResult(
				billingKey,
				method.path("name").asText("등록 카드"),
				fourCardNumber
		);
	}

	@Override
	public PayResult pay(PayRequest request) {
		if (properties.isStubEnabled()) {
			return stubPay(request.paymentId(), "PAID");
		}
		validateLiveConfiguration();

		Map<String, Object> body = Map.of(
				"storeId", properties.getStoreId(),
				"channelKey", resolveChannelKey(request.channelKey()),
				"orderName", request.orderName(),
				"amount", Map.of("total", request.totalAmount()),
				"currency", request.currency()
		);
		JsonNode response = post("/payments/" + request.paymentId() + "/instant", body);
		return toPayResult(request.paymentId(), response.path("payment"));
	}

	@Override
	public PayResult payWithBillingKey(PayWithBillingKeyRequest request) {
		if (properties.isStubEnabled()) {
			return stubPay(request.paymentId(), "PAID");
		}
		validateLiveConfiguration();

		Map<String, Object> body = Map.of(
				"storeId", properties.getStoreId(),
				"billingKey", request.billingKey(),
				"channelKey", resolveChannelKey(request.channelKey()),
				"orderName", request.orderName(),
				"amount", Map.of("total", request.totalAmount()),
				"currency", request.currency()
		);
		JsonNode response = post("/payments/" + request.paymentId() + "/billing-key", body);
		return toPayResult(request.paymentId(), response.path("payment"));
	}

	@Override
	public PaymentInquiryResult getPayment(String paymentId) {
		if (properties.isStubEnabled()) {
			return new PaymentInquiryResult(paymentId, "PAID", 0L, "stub-billing-key", Instant.now());
		}
		validateLiveConfiguration();

		JsonNode payment = get("/payments/" + paymentId);
		long totalAmount = payment.path("amount").path("total").asLong(0L);
		return new PaymentInquiryResult(
				paymentId,
				payment.path("status").asText("UNKNOWN"),
				totalAmount,
				payment.path("billingKey").asText(null),
				parseInstant(payment.path("paidAt").asText(null))
		);
	}

	@Override
	public CancelPaymentResult cancelPayment(CancelPaymentRequest request) {
		if (properties.isStubEnabled()) {
			return new CancelPaymentResult(request.paymentId(), "CANCELLED", Instant.now());
		}
		validateLiveConfiguration();

		Map<String, Object> body = request.cancelAmount() == null
				? Map.of("reason", request.reason())
				: Map.of("reason", request.reason(), "amount", request.cancelAmount());
		JsonNode response = post("/payments/" + request.paymentId() + "/cancel", body);
		JsonNode cancellation = response.path("cancellation");
		return new CancelPaymentResult(
				request.paymentId(),
				cancellation.path("status").asText("CANCELLED"),
				parseInstant(cancellation.path("cancelledAt").asText(null))
		);
	}

	private IssueBillingKeyResult stubIssueBillingKey(IssueBillingKeyRequest request) {
		String digits = request.cardCredential().number().replaceAll("\\D", "");
		String fourDigits = digits.length() >= 4 ? digits.substring(digits.length() - 4) : "0000";
		return new IssueBillingKeyResult(
				"stub-billing-key-" + UUID.randomUUID(),
				"Stub Card",
				fourDigits
		);
	}

	private PayResult stubPay(String paymentId, String status) {
		return new PayResult(paymentId, status, Instant.now());
	}

	private JsonNode post(String path, Object body) {
		try {
			String responseBody = restClient.post()
					.uri(path)
					.contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "PortOne " + properties.getApiSecret())
					.body(body)
					.retrieve()
					.onStatus(HttpStatusCode::isError, (request, response) -> {
						throw new PaymentGatewayException(
								"PortOne API call failed. path=" + path + ", status=" + response.getStatusCode()
						);
					})
					.body(String.class);
			return objectMapper.readTree(responseBody);
		} catch (PaymentGatewayException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new PaymentGatewayException("PortOne API call failed. path=" + path, exception);
		}
	}

	private JsonNode get(String path) {
		try {
			String responseBody = restClient.get()
					.uri(path)
					.header("Authorization", "PortOne " + properties.getApiSecret())
					.retrieve()
					.onStatus(HttpStatusCode::isError, (request, response) -> {
						throw new PaymentGatewayException(
								"PortOne API call failed. path=" + path + ", status=" + response.getStatusCode()
						);
					})
					.body(String.class);
			return objectMapper.readTree(responseBody);
		} catch (PaymentGatewayException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new PaymentGatewayException("PortOne API call failed. path=" + path, exception);
		}
	}

	private PayResult toPayResult(String paymentId, JsonNode payment) {
		return new PayResult(
				paymentId,
				payment.path("status").asText("UNKNOWN"),
				parseInstant(payment.path("paidAt").asText(null))
		);
	}

	private Instant parseInstant(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return Instant.parse(value);
	}

	private String resolveChannelKey(String requestChannelKey) {
		if (requestChannelKey != null && !requestChannelKey.isBlank()) {
			return requestChannelKey;
		}
		return properties.getChannelKey();
	}

	private void validateLiveConfiguration() {
		if (!properties.isConfiguredForLiveApi()) {
			throw new PaymentGatewayException(
					"PortOne live API is not configured. Set portone.stub-enabled=false and provide store-id, channel-key, api-secret."
			);
		}
	}
}
