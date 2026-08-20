package com.planwith.planwith_fo_token.application.service.support;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

@Component
public class TokenLedgerCommandExecutor {

	private final TokenWalletMutationExecutor mutationExecutor;
	private final TokenLedgerIdempotencySupport idempotencySupport;

	public TokenLedgerCommandExecutor(
			TokenWalletMutationExecutor mutationExecutor,
			TokenLedgerIdempotencySupport idempotencySupport
	) {
		this.mutationExecutor = mutationExecutor;
		this.idempotencySupport = idempotencySupport;
	}

	public Optional<TokenLedger> findProcessed(TransactionUuid transactionUuid) {
		TokenLedger existing = idempotencySupport.findExisting(transactionUuid);
		return Optional.ofNullable(existing);
	}

	public TokenLedger executeMutation(
			TransactionUuid transactionUuid,
			MemberUuid memberUuid,
			Function<TokenWallet, TokenLedger> mutation
	) {
		return mutationExecutor.execute(memberUuid, transactionUuid, mutation);
	}
}
