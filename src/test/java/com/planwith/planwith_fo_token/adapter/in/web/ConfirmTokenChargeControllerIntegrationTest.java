package com.planwith.planwith_fo_token.adapter.in.web;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_token.adapter.out.pg.portone.PortOnePaymentAdapter;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ConfirmTokenChargeControllerIntegrationTest {

	private static final UUID MEMBER = UUID.fromString("c2727272-2727-2727-2727-272727272727");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PortOnePaymentAdapter portOnePaymentAdapter;

	@Test
	void confirmEndpointVerifiesPgAndGrantsTokens() throws Exception {
		UUID chargeUuid = createReadyCharge(Map.of(
				"productCode", "BASIC",
				"paymentType", "ONE_TIME",
				"clientRequestId", "http-confirm-1"
		));
		portOnePaymentAdapter.putStubPayment(chargeUuid.toString(), "PAID", 4_900L);

		mockMvc.perform(post(
						"/api/planwith-fo-token/members/{memberUuid}/charges/{chargeUuid}/confirm",
						MEMBER,
						chargeUuid
				).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"providerPaymentId", chargeUuid.toString(),
								"paidAmount", 4900
						))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PAID"))
				.andExpect(jsonPath("$.tokenAmount").value(60))
				.andExpect(jsonPath("$.paidAmount").value(4900));
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
