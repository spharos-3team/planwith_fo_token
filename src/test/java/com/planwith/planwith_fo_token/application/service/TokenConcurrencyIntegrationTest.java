package com.planwith.planwith_fo_token.application.service;

import static org.assertj.core.api.Assertions.assertThat;

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

import com.planwith.planwith_fo_token.application.command.ChargeTokenCommand;
import com.planwith.planwith_fo_token.application.command.UseTokenCommand;
import com.planwith.planwith_fo_token.application.port.in.command.ChargeTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.command.UseTokenUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.TokenBalanceResult;
import com.planwith.planwith_fo_token.domain.exception.InsufficientTokenBalanceException;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

@SpringBootTest
@ActiveProfiles("test")
class TokenConcurrencyIntegrationTest {

	private static final MemberUuid MEMBER = MemberUuid.from("77777777-7777-7777-7777-777777777777");

	@Autowired
	private ChargeTokenUseCase chargeTokenUseCase;

	@Autowired
	private UseTokenUseCase useTokenUseCase;

	@Autowired
	private GetTokenBalanceQueryUseCase getTokenBalanceQueryUseCase;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Test
	void onlyOneConcurrentUseSucceedsWhenBalanceIsTen() throws Exception {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.executeWithoutResult(status -> chargeTokenUseCase.charge(new ChargeTokenCommand(
				TransactionUuid.from("faaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
				MEMBER,
				10L,
				"PAYMENT",
				"seed",
				"seed"
		)));

		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger insufficientCount = new AtomicInteger();
		CopyOnWriteArrayList<Throwable> errors = new CopyOnWriteArrayList<>();

		for (String txSuffix : new String[] {"bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", "cccccccc-cccc-cccc-cccc-cccccccccccc"}) {
			executor.submit(() -> {
				ready.countDown();
				try {
					start.await(5, TimeUnit.SECONDS);
					useTokenUseCase.use(new UseTokenCommand(
							TransactionUuid.from(txSuffix),
							MEMBER,
							10L,
							"AI_SCHEDULE",
							"schedule",
							"concurrent use"
					));
					successCount.incrementAndGet();
				} catch (InsufficientTokenBalanceException exception) {
					insufficientCount.incrementAndGet();
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
		assertThat(successCount.get()).isEqualTo(1);
		assertThat(insufficientCount.get()).isEqualTo(1);

		TokenBalanceResult balance = transactionTemplate.execute(status ->
				getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER))
		);
		assertThat(balance.totalBalance()).isZero();
	}

	@Test
	void duplicateIdempotencyKeyReturnsSameResultWithoutDoubleDeduction() throws Exception {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.executeWithoutResult(status -> chargeTokenUseCase.charge(new ChargeTokenCommand(
				TransactionUuid.from("fddddddd-dddd-dddd-dddd-dddddddddddd"),
				MEMBER,
				10L,
				"PAYMENT",
				"seed-dup",
				"seed"
		)));

		UseTokenCommand command = new UseTokenCommand(
				TransactionUuid.from("feeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
				MEMBER,
				4L,
				"PDF_DOWNLOAD",
				"pdf",
				"idempotent use"
		);

		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		AtomicInteger successCount = new AtomicInteger();
		CopyOnWriteArrayList<Throwable> errors = new CopyOnWriteArrayList<>();

		for (int i = 0; i < 2; i++) {
			executor.submit(() -> {
				ready.countDown();
				try {
					start.await(5, TimeUnit.SECONDS);
					useTokenUseCase.use(command);
					successCount.incrementAndGet();
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
		assertThat(successCount.get()).isEqualTo(2);

		TokenBalanceResult balance = transactionTemplate.execute(status ->
				getTokenBalanceQueryUseCase.getBalance(new GetTokenBalanceQuery(MEMBER))
		);
		assertThat(balance.totalBalance()).isEqualTo(6L);
	}
}
