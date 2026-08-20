package com.planwith.planwith_fo_token.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.ChargeTokenCommand;
import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;
import com.planwith.planwith_fo_token.application.command.UseTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.ChargeTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.GrantTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.UseTokenUseCase;
import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.application.port.out.TokenChargePort;
import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.PaymentMethodStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentType;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;
import com.planwith.planwith_fo_token.domain.model.vo.ChargeUuid;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TokenHistoryQueryControllerIntegrationTest {

	private static final UUID MEMBER = UUID.fromString("88888888-8888-8888-8888-888888888888");
	private static final MemberUuid MEMBER_UUID = MemberUuid.from(MEMBER.toString());

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ChargeTokenUseCase chargeTokenUseCase;

	@Autowired
	private GrantTokenUseCase grantTokenUseCase;

	@Autowired
	private UseTokenUseCase useTokenUseCase;

	@Autowired
	private PaymentMethodPort paymentMethodPort;

	@Autowired
	private TokenChargePort tokenChargePort;

	@Test
	void ledgerReturnsNewestFirstWithSignedAmountChangeAndTypeFilter() throws Exception {
		chargeTokenUseCase.charge(new ChargeTokenCommand(
				TransactionUuid.from("a1111111-1111-1111-1111-111111111111"),
				MEMBER_UUID,
				100L,
				"PAYMENT",
				"p1",
				"charge"
		));
		grantTokenUseCase.grant(new GrantTokenCommand(
				TransactionUuid.from("a2222222-2222-2222-2222-222222222222"),
				MEMBER_UUID,
				30L,
				"GRADE_REWARD",
				"g1",
				"reward"
		));
		useTokenUseCase.use(new UseTokenCommand(
				TransactionUuid.from("a3333333-3333-3333-3333-333333333333"),
				MEMBER_UUID,
				40L,
				"AI_SCHEDULE",
				"s1",
				"use"
		));

		mockMvc.perform(get("/api/planwith-fo-token/members/{memberUuid}/tokens/ledger", MEMBER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[0].transactionType").value("USE"))
				.andExpect(jsonPath("$[0].amountChange").value(-40))
				.andExpect(jsonPath("$[0].usagePlace").value("AI_SCHEDULE"))
				.andExpect(jsonPath("$[0].balanceAfter").exists())
				.andExpect(jsonPath("$[0].description").value("use"));

		mockMvc.perform(get("/api/planwith-fo-token/members/{memberUuid}/tokens/ledger", MEMBER)
						.param("type", "CHARGE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].transactionType").value("CHARGE"))
				.andExpect(jsonPath("$[0].amountChange").value(100));
	}

	@Test
	void chargeHistoryReturnsPaymentFieldsFromTokenChargeTable() throws Exception {
		Instant now = Instant.parse("2026-08-20T10:00:00Z");
		PaymentMethodUuid paymentMethodUuid = PaymentMethodUuid.from("b1111111-1111-1111-1111-111111111111");
		paymentMethodPort.save(PaymentMethod.restore(
				null,
				paymentMethodUuid,
				MEMBER_UUID,
				"billing-key",
				"신한카드",
				"1234",
				true,
				PaymentMethodStatus.ACTIVE,
				now
		));
		tokenChargePort.save(TokenCharge.restore(
				null,
				ChargeUuid.from("b2222222-2222-2222-2222-222222222222"),
				TransactionUuid.from("b3333333-3333-3333-3333-333333333333"),
				paymentMethodUuid,
				PaymentType.BILLING_KEY,
				"PAY-20260820-001",
				150L,
				"billing-key",
				4900L,
				ChargeStatus.PAID,
				now,
				now
		));

		mockMvc.perform(get("/api/planwith-fo-token/members/{memberUuid}/tokens/charges", MEMBER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].paymentCode").value("PAY-20260820-001"))
				.andExpect(jsonPath("$[0].tokenAmount").value(150))
				.andExpect(jsonPath("$[0].paidAmount").value(4900))
				.andExpect(jsonPath("$[0].paymentMethodName").value("신한카드"))
				.andExpect(jsonPath("$[0].cardLastFour").value("1234"))
				.andExpect(jsonPath("$[0].status").value("PAID"));
	}
}
