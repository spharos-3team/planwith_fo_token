package com.planwith.planwith_fo_token.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UseTokenControllerIntegrationTest {

	private static final UUID MEMBER = UUID.fromString("66666666-6666-6666-6666-666666666666");

	@Autowired
	private MockMvc mockMvc;

	@Test
	void useReturnsTokenInsufficientWhenBalanceIsEmpty() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-token/members/{memberUuid}/tokens/use", MEMBER)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "transactionUuid": "f1111111-1111-1111-1111-111111111111",
								  "amount": 10,
								  "referenceType": "AI_SCHEDULE",
								  "referenceUuid": "schedule-1",
								  "description": "use"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("TOKEN_INSUFFICIENT"));
	}

	@Test
	void useDeductsTokensForAiSchedule() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-token/members/{memberUuid}/tokens/charge", MEMBER)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "transactionUuid": "f2222222-2222-2222-2222-222222222222",
								  "amount": 30,
								  "referenceType": "PAYMENT",
								  "referenceUuid": "pay-1",
								  "description": "charge"
								}
								"""))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/planwith-fo-token/members/{memberUuid}/tokens/use", MEMBER)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "transactionUuid": "f3333333-3333-3333-3333-333333333333",
								  "amount": 12,
								  "referenceType": "PDF_DOWNLOAD",
								  "referenceUuid": "pdf-1",
								  "description": "pdf"
								}
								"""))
				.andExpect(status().isNoContent());
	}
}
