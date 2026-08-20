package com.planwith.planwith_fo_token.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.planwith.planwith_fo_token.application.command.HandleGradeRewardGrantedCommand;
import com.planwith.planwith_fo_token.application.command.HandlePaymentCompletedCommand;
import com.planwith.planwith_fo_token.application.port.in.HandleGradeRewardGrantedUseCase;
import com.planwith.planwith_fo_token.application.port.in.HandlePaymentCompletedUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.ProcessedTokenEventPort;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
class TokenEventIdempotencyIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("99999999-9999-9999-9999-999999999999");

	@Autowired
	private HandlePaymentCompletedUseCase handlePaymentCompletedUseCase;

	@Autowired
	private HandleGradeRewardGrantedUseCase handleGradeRewardGrantedUseCase;

	@Autowired
	private GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase;

	@Autowired
	private ProcessedTokenEventPort processedTokenEventPort;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Test
	void duplicatePaymentCompletedEventIsProcessedOnce() throws Exception {
		UUID eventUuid = UUID.fromString("11112222-3333-4444-5555-666677778888");
		HandlePaymentCompletedCommand command = new HandlePaymentCompletedCommand(
				eventUuid,
				MEMBER,
				50L,
				"payment-ref",
				Instant.parse("2026-01-01T00:00:00Z")
		);

		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		AtomicInteger completed = new AtomicInteger();
		CopyOnWriteArrayList<Throwable> errors = new CopyOnWriteArrayList<>();

		for (int i = 0; i < 2; i++) {
			executor.submit(() -> {
				ready.countDown();
				try {
					start.await(5, TimeUnit.SECONDS);
					handlePaymentCompletedUseCase.handle(command);
					completed.incrementAndGet();
				} catch (Exception exception) {
					errors.add(exception);
				}
			});
		}

		assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
		start.countDown();
		executor.shutdown();
		assertThat(executor.awaitTermination(30, TimeUnit.SECONDS)).isTrue();
		assertThat(errors).isEmpty();
		assertThat(completed.get()).isEqualTo(2);

		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		Long paidBalance = transactionTemplate.execute(status ->
				getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).paidBalance()
		);
		assertThat(paidBalance).isEqualTo(50L);
		assertThat(processedTokenEventPort.existsByEventUuid(eventUuid)).isTrue();
	}

	@Test
	void duplicateGradeRewardEventIsProcessedOnce() throws Exception {
		UUID eventUuid = UUID.fromString("22223333-4444-5555-6666-777788889999");
		HandleGradeRewardGrantedCommand command = new HandleGradeRewardGrantedCommand(
				eventUuid,
				MEMBER,
				15L,
				"GOLD",
				Instant.parse("2026-02-01T00:00:00Z")
		);

		handleGradeRewardGrantedUseCase.handle(command);
		handleGradeRewardGrantedUseCase.handle(command);

		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		Long freeBalance = transactionTemplate.execute(status ->
				getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER)).freeBalance()
		);
		assertThat(freeBalance).isEqualTo(15L);
		assertThat(processedTokenEventPort.existsByEventUuid(eventUuid)).isTrue();
	}
}
