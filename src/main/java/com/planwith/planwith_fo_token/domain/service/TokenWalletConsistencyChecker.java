package com.planwith.planwith_fo_token.domain.service;

import java.util.List;
import java.util.Objects;

import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;

/**
 * Wallet 재구성 잔액과 Ledger 최종 balanceAfter 정합성을 검증한다.
 */
public final class TokenWalletConsistencyChecker {

	private TokenWalletConsistencyChecker() {
	}

	public static ConsistencyResult check(MemberUuid memberUuid, List<TokenLedger> chronologicalEntries) {
		Objects.requireNonNull(memberUuid, "Member UUID is required.");
		List<TokenLedger> ledgers = chronologicalEntries == null ? List.of() : chronologicalEntries;
		TokenWallet reconstructed = TokenWalletReconstructor.reconstruct(memberUuid, ledgers);
		long walletTotal = reconstructed.getTotalBalance();
		if (ledgers.isEmpty()) {
			return ConsistencyResult.consistent(memberUuid, walletTotal, 0L, 0);
		}
		TokenLedger last = ledgers.get(ledgers.size() - 1);
		long ledgerBalanceAfter = last.balanceAfter();
		if (walletTotal == ledgerBalanceAfter) {
			return ConsistencyResult.consistent(memberUuid, walletTotal, ledgerBalanceAfter, ledgers.size());
		}
		return ConsistencyResult.inconsistent(memberUuid, walletTotal, ledgerBalanceAfter, ledgers.size());
	}

	public record ConsistencyResult(
			MemberUuid memberUuid,
			boolean consistent,
			long walletTotalBalance,
			long ledgerBalanceAfter,
			int ledgerCount
	) {
		public static ConsistencyResult consistent(
				MemberUuid memberUuid,
				long walletTotalBalance,
				long ledgerBalanceAfter,
				int ledgerCount
		) {
			return new ConsistencyResult(memberUuid, true, walletTotalBalance, ledgerBalanceAfter, ledgerCount);
		}

		public static ConsistencyResult inconsistent(
				MemberUuid memberUuid,
				long walletTotalBalance,
				long ledgerBalanceAfter,
				int ledgerCount
		) {
			return new ConsistencyResult(memberUuid, false, walletTotalBalance, ledgerBalanceAfter, ledgerCount);
		}
	}
}
