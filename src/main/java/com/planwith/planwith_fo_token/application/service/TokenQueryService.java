package com.planwith.planwith_fo_token.application.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenChargeHistoryQueryUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenLedgerQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.LoadTokenChargePort;
import com.planwith.planwith_fo_token.application.port.out.LoadTokenLedgerPort;
import com.planwith.planwith_fo_token.application.port.out.LoadTokenWalletPort;
import com.planwith.planwith_fo_token.application.port.out.PaymentMethodPort;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.GetTokenChargeHistoryQuery;
import com.planwith.planwith_fo_token.application.query.GetTokenLedgerQuery;
import com.planwith.planwith_fo_token.application.query.TokenBalanceResult;
import com.planwith.planwith_fo_token.application.query.TokenChargeHistoryResult;
import com.planwith.planwith_fo_token.application.query.TokenLedgerEntryResult;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.TokenCharge;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.TransactionType;

@Service
public class TokenQueryService implements
		GetTokenBalanceQueryUseCase,
		GetTokenLedgerQueryUseCase,
		GetTokenChargeHistoryQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(TokenQueryService.class);

	private final LoadTokenWalletPort loadTokenWalletPort;
	private final LoadTokenLedgerPort loadTokenLedgerPort;
	private final LoadTokenChargePort loadTokenChargePort;
	private final PaymentMethodPort paymentMethodPort;

	public TokenQueryService(
			LoadTokenWalletPort loadTokenWalletPort,
			LoadTokenLedgerPort loadTokenLedgerPort,
			LoadTokenChargePort loadTokenChargePort,
			PaymentMethodPort paymentMethodPort
	) {
		this.loadTokenWalletPort = loadTokenWalletPort;
		this.loadTokenLedgerPort = loadTokenLedgerPort;
		this.loadTokenChargePort = loadTokenChargePort;
		this.paymentMethodPort = paymentMethodPort;
	}

	@Override
	@Transactional(readOnly = true)
	public TokenBalanceResult getBalance(GetTokenBalanceQuery query) {
		log.info("TokenQueryService : getBalance : 토큰 잔액 조회 - memberUuid={}", query.memberUuid());
		TokenWallet wallet = loadTokenWalletPort.load(query.memberUuid());
		TokenBalanceResult result = new TokenBalanceResult(
				query.memberUuid(),
				wallet.paidBalance(),
				wallet.freeBalance(),
				wallet.bonusBalance(),
				wallet.totalBalance()
		);
		log.info(
				"TokenQueryService : getBalance : 토큰 잔액 조회 완료 - memberUuid={}, totalBalance={}, paid={}, free={}, bonus={}",
				query.memberUuid(),
				result.totalBalance(),
				result.paidBalance(),
				result.freeBalance(),
				result.bonusBalance()
		);
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<TokenLedgerEntryResult> getLedger(GetTokenLedgerQuery query) {
		log.info(
				"TokenQueryService : getLedger : 토큰 거래 내역 조회 - memberUuid={}, transactionType={}, page={}, size={}",
				query.memberUuid(),
				query.transactionType(),
				query.page(),
				query.size()
		);
		List<TokenLedger> ledgers = query.transactionType() == null
				? loadTokenLedgerPort.findByMemberUuid(query.memberUuid(), query.page(), query.size())
				: loadTokenLedgerPort.findByMemberUuidAndEntryType(
						query.memberUuid(),
						query.transactionType(),
						query.page(),
						query.size()
				);
		List<TokenLedgerEntryResult> results = ledgers.stream()
				.map(this::toLedgerResult)
				.toList();
		log.info("TokenQueryService : getLedger : 토큰 거래 내역 조회 완료 - memberUuid={}, count={}",
				query.memberUuid(), results.size());
		return results;
	}

	@Override
	@Transactional(readOnly = true)
	public List<TokenChargeHistoryResult> getChargeHistory(GetTokenChargeHistoryQuery query) {
		log.info(
				"TokenQueryService : getChargeHistory : 토큰 충전 내역 조회 - memberUuid={}, page={}, size={}",
				query.memberUuid(),
				query.page(),
				query.size()
		);
		List<TokenChargeHistoryResult> results = loadTokenChargePort.findByMemberUuid(
						query.memberUuid(),
						query.page(),
						query.size()
				)
				.stream()
				.map(this::toChargeHistoryResult)
				.toList();
		log.info("TokenQueryService : getChargeHistory : 토큰 충전 내역 조회 완료 - memberUuid={}, count={}",
				query.memberUuid(), results.size());
		return results;
	}

	private TokenLedgerEntryResult toLedgerResult(TokenLedger entry) {
		return new TokenLedgerEntryResult(
				entry.ledgerId(),
				entry.transactionUuid(),
				entry.memberUuid(),
				entry.occurredAt(),
				entry.transactionType(),
				entry.tokenType(),
				entry.amount(),
				toAmountChange(entry.transactionType(), entry.amount()),
				entry.balanceAfter(),
				entry.referenceType(),
				entry.description()
		);
	}

	private TokenChargeHistoryResult toChargeHistoryResult(TokenCharge charge) {
		PaymentMethod paymentMethod = charge.paymentMethodUuid() == null
				? null
				: paymentMethodPort.findByUuid(charge.paymentMethodUuid().value()).orElse(null);
		String paymentCode = charge.providerPaymentId() != null && !charge.providerPaymentId().isBlank()
				? charge.providerPaymentId()
				: charge.chargeUuid().toString();
		return new TokenChargeHistoryResult(
				charge.chargeUuid().value(),
				paymentCode,
				charge.chargedAt() != null ? charge.chargedAt() : charge.createdAt(),
				charge.tokenAmount(),
				charge.paidAmount(),
				charge.paymentType(),
				paymentMethod == null ? null : paymentMethod.cardName(),
				paymentMethod == null ? null : paymentMethod.fourCardNumber(),
				charge.status()
		);
	}

	private static long toAmountChange(TransactionType transactionType, long amount) {
		return switch (transactionType) {
			case CHARGE, REWARD -> amount;
			case USE, EXPIRE -> -amount;
		};
	}
}
