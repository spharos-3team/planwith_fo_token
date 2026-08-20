package com.planwith.planwith_fo_token.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TokenBalanceQueryControllerIntegrationTest {

	private static final UUID MEMBER = UUID.fromString("55555555-5555-5555-5555-555555555555");
	private static final UUID NEW_MEMBER = UUID.fromString("66666666-6666-6666-6666-666666666666");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void newMemberReturnsZeroBalances() throws Exception {
		mockMvc.perform(get("/api/planwith-fo-token/members/{memberUuid}/tokens/balance", NEW_MEMBER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalBalance").value(0))
				.andExpect(jsonPath("$.paidBalance").value(0))
				.andExpect(jsonPath("$.freeBalance").value(0))
				.andExpect(jsonPath("$.bonusBalance").value(0));
	}

	@Test
	void returnsPaidFreeBonusAndTotalBalances() throws Exception {
		seedMemberBalance(MEMBER);

		mockMvc.perform(get("/api/planwith-fo-token/members/{memberUuid}/tokens/balance", MEMBER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalBalance").value(150))
				.andExpect(jsonPath("$.paidBalance").value(80))
				.andExpect(jsonPath("$.freeBalance").value(50))
				.andExpect(jsonPath("$.bonusBalance").value(20));
	}

	@Test
	void headerSummaryReturnsTotalBalanceOnly() throws Exception {
		seedMemberBalance(MEMBER);

		mockMvc.perform(get("/api/planwith-fo-token/members/{memberUuid}/tokens/balance/summary", MEMBER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalBalance").value(150))
				.andExpect(jsonPath("$.paidBalance").doesNotExist());
	}

	@Test
	void internalBalanceApiReturnsSameBalancesAsPublicApi() throws Exception {
		seedMemberBalance(MEMBER);

		String publicBody = mockMvc.perform(get("/api/planwith-fo-token/members/{memberUuid}/tokens/balance", MEMBER))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String internalBody = mockMvc.perform(
						get("/internal/planwith-fo-token/v1/members/{memberUuid}/tokens/balance", MEMBER))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		org.assertj.core.api.Assertions.assertThat(objectMapper.readTree(internalBody))
				.isEqualTo(objectMapper.readTree(publicBody));
	}

	private void seedMemberBalance(UUID memberUuid) throws Exception {
		mockMvc.perform(post("/api/planwith-fo-token/members/{memberUuid}/tokens/charge", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "transactionUuid": "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee",
								  "amount": 80,
								  "referenceType": "PAYMENT",
								  "referenceUuid": "payment-1",
								  "description": "paid charge"
								}
								"""))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/planwith-fo-token/members/{memberUuid}/tokens/grant", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "transactionUuid": "ffffffff-ffff-ffff-ffff-ffffffffffff",
								  "amount": 50,
								  "referenceType": "GRADE_REWARD",
								  "referenceUuid": "grade-1",
								  "description": "free grant"
								}
								"""))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/planwith-fo-token/members/{memberUuid}/tokens/grant", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "transactionUuid": "11111111-1111-1111-1111-111111111112",
								  "amount": 20,
								  "referenceType": "PAYMENT",
								  "referenceUuid": "bonus-1",
								  "description": "bonus grant"
								}
								"""))
				.andExpect(status().isNoContent());
	}
}
