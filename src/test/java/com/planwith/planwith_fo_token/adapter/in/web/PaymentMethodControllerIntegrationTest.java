package com.planwith.planwith_fo_token.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaymentMethodControllerIntegrationTest {

	private static final UUID MEMBER = UUID.fromString("f2222222-2222-2222-2222-222222222222");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void registerAndListPaymentMethodsWithoutSensitiveFields() throws Exception {
		String body = objectMapper.writeValueAsString(Map.of(
				"cardNumber", "4111111111111111",
				"expiryYear", "28",
				"expiryMonth", "12",
				"birthOrBusinessRegistrationNumber", "900101",
				"passwordTwoDigits", "12",
				"defaultMethod", true
		));

		mockMvc.perform(post("/api/planwith-fo-token/members/{memberUuid}/payment-methods", MEMBER)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.paymentMethodUuid").exists())
				.andExpect(jsonPath("$.cardName").value("Stub Card"))
				.andExpect(jsonPath("$.fourCardNumber").value("1111"))
				.andExpect(jsonPath("$.defaultMethod").value(true))
				.andExpect(jsonPath("$.billingKey").doesNotExist())
				.andExpect(jsonPath("$.cardNumber").doesNotExist())
				.andExpect(jsonPath("$.passwordTwoDigits").doesNotExist())
				.andExpect(jsonPath("$.expiryYear").doesNotExist());

		mockMvc.perform(get("/api/planwith-fo-token/members/{memberUuid}/payment-methods", MEMBER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].cardName").value("Stub Card"))
				.andExpect(jsonPath("$[0].fourCardNumber").value("1111"))
				.andExpect(jsonPath("$[0].defaultMethod").value(true))
				.andExpect(jsonPath("$[0].billingKey").doesNotExist());
	}

	@Test
	void registerRejectsMissingCardNumber() throws Exception {
		String body = objectMapper.writeValueAsString(Map.of(
				"expiryYear", "28",
				"expiryMonth", "12",
				"birthOrBusinessRegistrationNumber", "900101",
				"passwordTwoDigits", "12"
		));

		mockMvc.perform(post("/api/planwith-fo-token/members/{memberUuid}/payment-methods", MEMBER)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}
}
