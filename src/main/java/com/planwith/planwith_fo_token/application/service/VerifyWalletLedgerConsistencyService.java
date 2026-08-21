package com.planwith.planwith_fo_token.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.in.query.VerifyWalletLedgerConsistencyQueryUseCase;
import com.planwith.planwith_fo_token.application.port.out.LoadTokenLedgerPort;
import com.planwith.planwith_fo_token.application.query.VerifyWalletLedgerConsistencyQuery;
import com.planwith.planwith_fo_token.application.query.WalletLedgerConsistencyResult;
import com.planwith.planwith_fo_token.domain.exception.WalletLedgerInconsistencyException;
import com.planwith.planwith_fo_token.domain.service.TokenWalletConsistencyChecker;

@Service
public class VerifyWalletLedgerConsistencyService implements VerifyWalletLedgerConsistencyQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(VerifyWalletLedgerConsistencyService.class);

	private final LoadTokenLedgerPort loadTokenLedgerPort;

	public VerifyWalletLedgerConsistencyService(LoadTokenLedgerPort loadTokenLedgerPort) {
		this.loadTokenLedgerPort = loadTokenLedgerPort;
	}

	@Override
	@Transactional(readOnly = true)
	public WalletLedgerConsistencyResult verify(VerifyWalletLedgerConsistencyQuery query) {
		log.info(
				"VerifyWalletLedgerConsistencyService : verify : Wallet-Ledger 정합성 검증 요청 - memberUuid={}",
				query.memberUuid()
		);
		TokenWalletConsistencyChecker.ConsistencyResult result = TokenWalletConsistencyChecker.check(
				query.memberUuid(),
				loadTokenLedgerPort.findByMemberUuidChronological(query.memberUuid())
		);
		WalletLedgerConsistencyResult response = new WalletLedgerConsistencyResult(
				query.memberUuid().value(),
				result.consistent(),
				result.walletTotalBalance(),
				result.ledgerBalanceAfter(),
				result.ledgerCount()
		);
		if (!result.consistent()) {
			log.warn(
					"VerifyWalletLedgerConsistencyService : verify : Wallet-Ledger 불일치 검출 - memberUuid={}, walletTotal={}, ledgerBalanceAfter={}",
					query.memberUuid(),
					result.walletTotalBalance(),
					result.ledgerBalanceAfter()
			);
			throw new WalletLedgerInconsistencyException(
					"Wallet and ledger balances are inconsistent. memberUuid=" + query.memberUuid()
							+ ", walletTotal=" + result.walletTotalBalance()
							+ ", ledgerBalanceAfter=" + result.ledgerBalanceAfter()
			);
		}
		log.info(
				"VerifyWalletLedgerConsistencyService : verify : Wallet-Ledger 정합성 확인 - memberUuid={}, total={}, ledgerCount={}",
				query.memberUuid(),
				result.walletTotalBalance(),
				result.ledgerCount()
		);
		return response;
	}
}
