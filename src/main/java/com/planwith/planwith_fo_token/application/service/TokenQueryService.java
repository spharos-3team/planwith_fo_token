package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.in.query.GetTokenBalanceQueryUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenChargeHistoryQueryUseCase;
import com.planwith.planwith_fo_token.application.port.in.query.GetTokenLedgerQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.LoadTokenLedgerPort;
import com.planwith.planwith_fo_token.application.port.out.LoadTokenWalletPort;
import com.planwith.planwith_fo_token.application.query.GetTokenBalanceQuery;
import com.planwith.planwith_fo_token.application.query.GetTokenChargeHistoryQuery;
import com.planwith.planwith_fo_token.application.query.GetTokenLedgerQuery;
import com.planwith.planwith_fo_token.application.query.TokenBalanceResult;
import com.planwith.planwith_fo_token.application.query.TokenLedgerEntryResult;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TransactionType;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;

import java.util.List;

@Service
public class TokenQueryService implements
		GetTokenBalanceQueryUseCase,
		GetTokenLedgerQueryUseCase,
		GetTokenChargeHistoryQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(TokenQueryService.class);

	private final LoadTokenWalletPort loadTokenWalletPort;
	private final LoadTokenLedgerPort loadTokenLedgerPort;

	public TokenQueryService(LoadTokenWalletPort loadTokenWalletPort, LoadTokenLedgerPort loadTokenLedgerPort) {
		this.loadTokenWalletPort = loadTokenWalletPort;
		this.loadTokenLedgerPort = loadTokenLedgerPort;
	}

	@Override
	@Transactional(readOnly = true)
	public TokenBalanceResult getBalance(GetTokenBalanceQuery query) {
		log.debug("TokenQueryService : getBalance : 토큰 잔액 조회 - memberUuid={}", query.memberUuid());
		TokenWallet wallet = loadTokenWalletPort.load(query.memberUuid());
		return new TokenBalanceResult(
				query.memberUuid(),
				wallet.paidBalance(),
				wallet.freeBalance(),
				wallet.bonusBalance(),
				wallet.totalBalance()
		);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TokenLedgerEntryResult> getLedger(GetTokenLedgerQuery query) {
		log.debug("TokenQueryService : getLedger : 토큰 거래 내역 조회 - memberUuid={}", query.memberUuid());
		return loadTokenLedgerPort.findByMemberUuid(query.memberUuid(), query.page(), query.size())
				.stream()
				.map(this::toResult)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<TokenLedgerEntryResult> getChargeHistory(GetTokenChargeHistoryQuery query) {
		log.debug("TokenQueryService : getChargeHistory : 토큰 충전 내역 조회 - memberUuid={}", query.memberUuid());
		return loadTokenLedgerPort.findByMemberUuidAndEntryType(
						query.memberUuid(),
						TransactionType.CHARGE,
						query.page(),
						query.size()
				)
				.stream()
				.map(this::toResult)
				.toList();
	}

	private TokenLedgerEntryResult toResult(TokenLedger entry) {
		return new TokenLedgerEntryResult(
				entry.ledgerId(),
				entry.transactionUuid(),
				entry.memberUuid(),
				entry.entryType(),
				entry.tokenType(),
				entry.amount(),
				entry.balanceAfter(),
				entry.referenceType(),
				entry.description(),
				entry.occurredAt()
		);
	}
}
