package com.planwith.planwith_fo_token.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TokenChargeControllerIntegrationTest {

	private static final UUID MEMBER = UUID.fromString("a7171717-1717-1717-1717-171717171717");
	private static final UUID PAYMENT_METHOD = UUID.fromString("a8181818-1818-1818-1818-181818181818");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PaymentMethodPort paymentMethodPort;

	@Test
	void listProductsAndCreateReadyChargeBeforePg() throws Exception {
		paymentMethodPort.save(PaymentMethod.register(
				new PaymentMethodUuid(PAYMENT_METHOD),
				MemberUuid.from(MEMBER.toString()),
				"billing-key",
				"신한카드",
				"9999",
				true,
				Instant.parse("2026-08-21T00:00:00Z")
		));

		mockMvc.perform(get("/api/planwith-fo-token/members/{memberUuid}/token-products", MEMBER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(4))
				.andExpect(jsonPath("$[0].code").exists())
				.andExpect(jsonPath("$[?(@.code=='BASIC')].salePrice").value(org.hamcrest.Matchers.contains(4900)))
				.andExpect(jsonPath("$[?(@.code=='BASIC')].totalTokenAmount").value(org.hamcrest.Matchers.contains(60)));

		String body = objectMapper.writeValueAsString(Map.of(
				"productCode", "BASIC",
				"paymentMethodUuid", PAYMENT_METHOD.toString(),
				"paymentType", "BILLING_KEY",
				"clientRequestId", "client-req-1"
		));

		mockMvc.perform(post("/api/planwith-fo-token/members/{memberUuid}/charges", MEMBER)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.chargeUuid").exists())
				.andExpect(jsonPath("$.status").value("READY"))
				.andExpect(jsonPath("$.productCode").value("BASIC"))
				.andExpect(jsonPath("$.paidAmount").value(4900))
				.andExpect(jsonPath("$.tokenAmount").value(60))
				.andExpect(jsonPath("$.paymentMethodUuid").value(PAYMENT_METHOD.toString()));
	}
}
