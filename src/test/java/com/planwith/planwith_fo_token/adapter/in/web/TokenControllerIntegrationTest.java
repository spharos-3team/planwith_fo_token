package com.planwith.planwith_fo_token.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TokenControllerIntegrationTest {

	private static final UUID MEMBER = UUID.fromString("44444444-4444-4444-4444-444444444444");
	private static final UUID CHARGE_TX = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

	@Autowired
	private MockMvc mockMvc;

	@Test
	void controllerToPersistenceQueryAndCommandFlow() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-token/members/{memberUuid}/tokens/charge", MEMBER)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "transactionUuid": "%s",
								  "amount": 200,
								  "referenceType": "PAYMENT",
								  "referenceUuid": "payment-1",
								  "description": "charge"
								}
								""".formatted(CHARGE_TX)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/planwith-fo-token/members/{memberUuid}/tokens/balance", MEMBER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalBalance").value(200))
				.andExpect(jsonPath("$.paidBalance").value(200));

		mockMvc.perform(get("/api/planwith-fo-token/members/{memberUuid}/tokens/ledger", MEMBER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
	}
}
