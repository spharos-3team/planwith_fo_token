package com.planwith.planwith_fo_token.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.command.DeletePaymentMethodCommand;
import com.planwith.planwith_fo_token.application.command.HandleGradeRewardGrantedCommand;
import com.planwith.planwith_fo_token.application.command.PayTokenChargeCommand;
import com.planwith.planwith_fo_token.application.command.RegisterPaymentMethodCommand;
import com.planwith.planwith_fo_token.application.command.RequestTokenChargeCommand;
import com.planwith.planwith_fo_token.application.command.SetDefaultPaymentMethodCommand;
import com.planwith.planwith_fo_token.application.command.UseTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.HandleGradeRewardGrantedUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.DeletePaymentMethodUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.PayTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.RegisterPaymentMethodUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.RequestTokenChargeUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.SetDefaultPaymentMethodUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.UseTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenLedgerQueryUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.ListTokenProductsQueryUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.VerifyWalletLedgerConsistencyQueryUseCase;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.GetTokenLedgerQuery;
import com.planwith.planwith_fo_token.application.query.PaymentMethodResult;
import com.planwith.planwith_fo_token.application.query.TokenBalanceResult;
import com.planwith.planwith_fo_token.application.query.TokenChargeRequestResult;
import com.planwith.planwith_fo_token.application.query.TokenLedgerEntryResult;
import com.planwith.planwith_fo_token.application.query.VerifyWalletLedgerConsistencyQuery;
import com.planwith.planwith_fo_token.domain.exception.PaymentMethodNotFoundException;
import com.planwith.planwith_fo_token.domain.model.ChargeStatus;
import com.planwith.planwith_fo_token.domain.model.PaymentType;
import com.planwith.planwith_fo_token.domain.model.TransactionType;
import com.planwith.planwith_fo_token.domain.model.vo.ChargeUuid;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

/**
 * 카드 등록 → 결제 → PAID 지급 → 사용 → Ledger → FREE 만료/등급 지급 최종 사용자 흐름 검증.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FinalTokenPaymentLifecycleIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("d2222222-2222-2222-2222-222222222222");

	@Autowired
	private RegisterPaymentMethodUseCase registerPaymentMethodUseCase;

	@Autowired
	private SetDefaultPaymentMethodUseCase setDefaultPaymentMethodUseCase;

	@Autowired
	private DeletePaymentMethodUseCase deletePaymentMethodUseCase;

	@Autowired
	private ListTokenProductsQueryUseCase listTokenProductsQueryUseCase;

	@Autowired
	private RequestTokenChargeUseCase requestTokenChargeUseCase;

	@Autowired
	private PayTokenChargeUseCase payTokenChargeUseCase;

	@Autowired
	private UseTokenUseCase useTokenUseCase;

	@Autowired
	private GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase;

	@Autowired
	private GetTokenLedgerQueryUseCase getTokenLedgerQueryUseCase;

	@Autowired
	private HandleGradeRewardGrantedUseCase handleGradeRewardGrantedUseCase;

	@Autowired
	private VerifyWalletLedgerConsistencyQueryUseCase verifyWalletLedgerConsistencyQueryUseCase;

	@Test
	void endToEndCardPayGrantUseLedgerAndMonthlyFreeLifecycle() {
		PaymentMethodResult firstCard = registerPaymentMethodUseCase.register(new RegisterPaymentMethodCommand(
				MEMBER, "첫 번째 카드", "4111111111111111", "28", "12", "900101", "12", true
		));
		PaymentMethodResult secondCard = registerPaymentMethodUseCase.register(new RegisterPaymentMethodCommand(
				MEMBER, "두 번째 카드", "4222222222222222", "29", "01", "900101", "34", false
		));
		setDefaultPaymentMethodUseCase.setDefault(new SetDefaultPaymentMethodCommand(
				MEMBER,
				new PaymentMethodUuid(secondCard.paymentMethodUuid())
		));

		assertThat(listTokenProductsQueryUseCase.listProducts()).isNotEmpty();

		TokenChargeRequestResult ready = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER,
				"BASIC",
				new PaymentMethodUuid(secondCard.paymentMethodUuid()),
				PaymentType.BILLING_KEY,
				"final-e2e-charge-1"
		));
		assertThat(ready.status()).isEqualTo(ChargeStatus.READY);

		TokenChargeRequestResult paid = payTokenChargeUseCase.pay(new PayTokenChargeCommand(
				MEMBER,
				new ChargeUuid(ready.chargeUuid()),
				4_900L
		));
		assertThat(paid.status()).isEqualTo(ChargeStatus.PAID);

		TokenBalanceResult afterPay = getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER));
		assertThat(afterPay.paidBalance()).isEqualTo(60L);
		assertThat(afterPay.totalBalance()).isEqualTo(60L);

		useTokenUseCase.use(new UseTokenCommand(
				new TransactionUuid(UUID.fromString("d3333333-3333-3333-3333-333333333333")),
				MEMBER,
				15L,
				"AI_SCHEDULE",
				"final-ai-1",
				"AI schedule use"
		));

		TokenBalanceResult afterUse = getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER));
		assertThat(afterUse.paidBalance()).isEqualTo(45L);
		assertThat(afterUse.totalBalance()).isEqualTo(45L);

		List<TokenLedgerEntryResult> ledger = getTokenLedgerQueryUseCase.getLedger(
				new GetTokenLedgerQuery(MEMBER, null, 0, 20)
		);
		assertThat(ledger).extracting(TokenLedgerEntryResult::transactionType)
				.contains(TransactionType.CHARGE, TransactionType.USE);

		handleGradeRewardGrantedUseCase.handle(new HandleGradeRewardGrantedCommand(
				UUID.fromString("d4444444-4444-4444-4444-444444444444"),
				MEMBER,
				20L,
				"MONTHLY_FREE_TOKEN",
				"2026-08",
				"GOLD",
				Instant.parse("2026-08-01T00:00:00Z")
		));
		handleGradeRewardGrantedUseCase.handle(new HandleGradeRewardGrantedCommand(
				UUID.fromString("d5555555-5555-5555-5555-555555555555"),
				MEMBER,
				30L,
				"MONTHLY_FREE_TOKEN",
				"2026-09",
				"GOLD",
				Instant.parse("2026-09-01T00:00:00Z")
		));

		TokenBalanceResult afterMonthly = getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER));
		assertThat(afterMonthly.freeBalance()).isEqualTo(30L);
		assertThat(afterMonthly.paidBalance()).isEqualTo(45L);
		assertThat(afterMonthly.totalBalance()).isEqualTo(75L);

		List<TokenLedgerEntryResult> monthlyLedger = getTokenLedgerQueryUseCase.getLedger(
				new GetTokenLedgerQuery(MEMBER, null, 0, 20)
		);
		assertThat(monthlyLedger).extracting(TokenLedgerEntryResult::transactionType)
				.contains(TransactionType.REWARD, TransactionType.EXPIRE);

		assertThat(verifyWalletLedgerConsistencyQueryUseCase.verify(
				new VerifyWalletLedgerConsistencyQuery(MEMBER)
		).consistent()).isTrue();

		assertThat(firstCard.paymentMethodUuid()).isNotNull();
	}

	@Test
	void deletedCardCannotBeUsedForBillingKeyPayment() {
		PaymentMethodResult card = registerPaymentMethodUseCase.register(new RegisterPaymentMethodCommand(
				MEMBER, "삭제할 카드", "4333333333333333", "30", "06", "900101", "56", true
		));
		TokenChargeRequestResult ready = requestTokenChargeUseCase.request(new RequestTokenChargeCommand(
				MEMBER,
				"TRIAL",
				new PaymentMethodUuid(card.paymentMethodUuid()),
				PaymentType.BILLING_KEY,
				"final-deleted-card-1"
		));

		deletePaymentMethodUseCase.delete(new DeletePaymentMethodCommand(
				MEMBER,
				new PaymentMethodUuid(card.paymentMethodUuid())
		));

		assertThatThrownBy(() -> payTokenChargeUseCase.pay(new PayTokenChargeCommand(
				MEMBER,
				new ChargeUuid(ready.chargeUuid()),
				1_000L
		))).isInstanceOf(PaymentMethodNotFoundException.class);
	}
}
