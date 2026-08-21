package com.planwith.planwith_fo_token.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PayTokenChargeControllerIntegrationTest {

	private static final UUID MEMBER = UUID.fromString("b3636363-3636-3636-3636-363636363636");
	private static final UUID PAYMENT_METHOD = UUID.fromString("b4646464-4646-4646-4646-464646464646");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PaymentMethodPort paymentMethodPort;

	@Test
	void billingKeyAndOneTimePayEndpointsShareTokenChargeFlow() throws Exception {
		paymentMethodPort.save(PaymentMethod.register(
				new PaymentMethodUuid(PAYMENT_METHOD),
				MemberUuid.from(MEMBER.toString()),
				"billing-key",
				"신한카드",
				"1111",
				true,
				Instant.parse("2026-08-21T01:00:00Z")
		));

		UUID billingChargeUuid = createReadyCharge(Map.of(
				"productCode", "BASIC",
				"paymentMethodUuid", PAYMENT_METHOD.toString(),
				"paymentType", "BILLING_KEY",
				"clientRequestId", "http-billing-1"
		));
		mockMvc.perform(post(
						"/api/planwith-fo-token/members/{memberUuid}/charges/{chargeUuid}/pay",
						MEMBER,
						billingChargeUuid
				).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("paidAmount", 4900))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PAID"))
				.andExpect(jsonPath("$.tokenAmount").value(60))
				.andExpect(jsonPath("$.paymentType").value("BILLING_KEY"));

		UUID oneTimeChargeUuid = createReadyCharge(Map.of(
				"productCode", "TRIAL",
				"paymentType", "ONE_TIME",
				"clientRequestId", "http-one-time-1"
		));
		mockMvc.perform(post(
						"/api/planwith-fo-token/members/{memberUuid}/charges/{chargeUuid}/pay",
						MEMBER,
						oneTimeChargeUuid
				))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PAID"))
				.andExpect(jsonPath("$.tokenAmount").value(10))
				.andExpect(jsonPath("$.paymentType").value("ONE_TIME"));
	}

	private UUID createReadyCharge(Map<String, Object> body) throws Exception {
		String response = mockMvc.perform(post("/api/planwith-fo-token/members/{memberUuid}/charges", MEMBER)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("READY"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		JsonNode node = objectMapper.readTree(response);
		return UUID.fromString(node.get("chargeUuid").asText());
	}
}
