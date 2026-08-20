package com.planwith.planwith_fo_token.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.planwith.planwith_fo_token.application.command.ChargeTokenCommand;
import com.planwith.planwith_fo_token.application.command.ExpireTokenCommand;
import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;
import com.planwith.planwith_fo_token.application.command.UseTokenCommand;
import com.planwith.planwith_fo_token.application.event.TokenChargedEvent;
import com.planwith.planwith_fo_token.application.event.TokenExpiredEvent;
import com.planwith.planwith_fo_token.application.event.TokenUsedEvent;
import com.planwith.planwith_fo_token.application.port.in.command.ChargeTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.ExpireTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.GrantTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.UseTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.LoadTokenLedgerPort;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

@SpringBootTest
@ActiveProfiles("test")
class TokenTransactionalOutboxFlowIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

	@Autowired
	private ChargeTokenUseCase chargeTokenUseCase;

	@Autowired
	private GrantTokenUseCase grantTokenUseCase;

	@Autowired
	private UseTokenUseCase useTokenUseCase;

	@Autowired
	private ExpireTokenUseCase expireTokenUseCase;

	@Autowired
	private GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase;

	@Autowired
	private LoadTokenLedgerPort loadTokenLedgerPort;

	@Autowired
	private SpringDataTokenOutboxRepository outboxRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Test
	void mutationPersistsLedgerAndOutboxTogetherWhileKafkaIsUnavailable() {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		UUID chargeTx = UUID.fromString("c1111111-1111-1111-1111-111111111111");
		UUID useTx = UUID.fromString("c2222222-2222-2222-2222-222222222222");

		transactionTemplate.executeWithoutResult(status -> chargeTokenUseCase.charge(new ChargeTokenCommand(
				new TransactionUuid(chargeTx),
				MEMBER,
				50L,
				"PAYMENT",
				"pay-1",
				"charge"
		)));
		transactionTemplate.executeWithoutResult(status -> useTokenUseCase.use(new UseTokenCommand(
				new TransactionUuid(useTx),
				MEMBER,
				20L,
				"AI_SCHEDULE",
				"schedule-1",
				"use"
		)));

		assertThat(loadTokenLedgerPort.findByTransactionUuid(new TransactionUuid(chargeTx))).isPresent();
		assertThat(loadTokenLedgerPort.findByTransactionUuid(new TransactionUuid(useTx))).isPresent();

		TokenOutboxJpaEntity chargeOutbox = outboxRepository.findByEventUuid(chargeTx).orElseThrow();
		TokenOutboxJpaEntity useOutbox = outboxRepository.findByEventUuid(useTx).orElseThrow();
		assertThat(chargeOutbox.eventType()).isEqualTo(TokenChargedEvent.EVENT_TYPE);
		assertThat(useOutbox.eventType()).isEqualTo(TokenUsedEvent.EVENT_TYPE);
		assertThat(chargeOutbox.publishedAt()).isNull();
		assertThat(useOutbox.publishedAt()).isNull();

		Long balance = transactionTemplate.execute(status ->
				getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).totalBalance()
		);
		assertThat(balance).isEqualTo(30L);
	}

	@Test
	void expireCreatesTokenExpiredOutboxEntry() {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		UUID grantTx = UUID.fromString("c3333333-3333-3333-3333-333333333333");
		UUID expireTx = UUID.fromString("c4444444-4444-4444-4444-444444444444");

		transactionTemplate.executeWithoutResult(status -> grantTokenUseCase.grant(GrantTokenCommand.gradeReward(
				new TransactionUuid(grantTx),
				MEMBER,
				15L,
				"GOLD",
				"grade"
		)));
		transactionTemplate.executeWithoutResult(status -> expireTokenUseCase.expire(new ExpireTokenCommand(
				new TransactionUuid(expireTx),
				MEMBER
		)));

		TokenOutboxJpaEntity expireOutbox = outboxRepository.findByEventUuid(expireTx).orElseThrow();
		assertThat(expireOutbox.eventType()).isEqualTo(TokenExpiredEvent.EVENT_TYPE);
		assertThat(expireOutbox.publishedAt()).isNull();
	}
}
