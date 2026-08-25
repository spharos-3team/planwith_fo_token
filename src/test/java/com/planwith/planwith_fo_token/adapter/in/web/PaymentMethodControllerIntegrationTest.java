package com.planwith.planwith_fo_token.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
				"cardName", "생활비 카드",
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
				.andExpect(jsonPath("$.cardName").value("생활비 카드"))
				.andExpect(jsonPath("$.fourCardNumber").value("1111"))
				.andExpect(jsonPath("$.defaultMethod").value(true))
				.andExpect(jsonPath("$.billingKey").doesNotExist())
				.andExpect(jsonPath("$.cardNumber").doesNotExist())
				.andExpect(jsonPath("$.passwordTwoDigits").doesNotExist())
				.andExpect(jsonPath("$.expiryYear").doesNotExist());

		mockMvc.perform(get("/api/planwith-fo-token/members/{memberUuid}/payment-methods", MEMBER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].cardName").value("생활비 카드"))
				.andExpect(jsonPath("$[0].fourCardNumber").value("1111"))
				.andExpect(jsonPath("$[0].defaultMethod").value(true))
				.andExpect(jsonPath("$[0].billingKey").doesNotExist());
	}

	@Test
	void registerRejectsMissingCardNumber() throws Exception {
		String body = objectMapper.writeValueAsString(Map.of(
				"cardName", "생활비 카드",
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

	@Test
	void registerRejectsMissingCardName() throws Exception {
		String body = objectMapper.writeValueAsString(Map.of(
				"cardNumber", "4111111111111111",
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

	@Test
	void setDefaultAndDeleteCompleteCardManagementScenario() throws Exception {
		UUID firstUuid = registerCard(MEMBER, "4111111111111111", true);
		UUID secondUuid = registerCard(MEMBER, "4222222222222222", false);

		mockMvc.perform(post(
						"/api/planwith-fo-token/members/{memberUuid}/payment-methods/{paymentMethodUuid}/default",
						MEMBER,
						secondUuid
				))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paymentMethodUuid").value(secondUuid.toString()))
				.andExpect(jsonPath("$.defaultMethod").value(true))
				.andExpect(jsonPath("$.billingKey").doesNotExist());

		mockMvc.perform(delete(
						"/api/planwith-fo-token/members/{memberUuid}/payment-methods/{paymentMethodUuid}",
						MEMBER,
						secondUuid
				))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/planwith-fo-token/members/{memberUuid}/payment-methods", MEMBER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].paymentMethodUuid").value(firstUuid.toString()))
				.andExpect(jsonPath("$[0].defaultMethod").value(true));
	}

	private UUID registerCard(UUID memberUuid, String cardNumber, boolean defaultMethod) throws Exception {
		String body = objectMapper.writeValueAsString(Map.of(
				"cardName", "테스트 카드",
				"cardNumber", cardNumber,
				"expiryYear", "28",
				"expiryMonth", "12",
				"birthOrBusinessRegistrationNumber", "900101",
				"passwordTwoDigits", "12",
				"defaultMethod", defaultMethod
		));
		String response = mockMvc.perform(post("/api/planwith-fo-token/members/{memberUuid}/payment-methods", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return UUID.fromString(objectMapper.readTree(response).get("paymentMethodUuid").asText());
	}
}
