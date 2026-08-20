package com.planwith.planwith_fo_token.application.service.support;

import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_token.application.port.out.LoadTokenWalletPort;
import com.planwith.planwith_fo_token.application.port.out.TokenWalletConcurrencyPort;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.vo.IdempotencyKey;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

@Component
public class TokenWalletMutationExecutor {

	private final LoadTokenWalletPort loadTokenWalletPort;
	private final TokenWalletConcurrencyPort concurrencyPort;
	private final TokenLedgerIdempotencySupport idempotencySupport;

	public TokenWalletMutationExecutor(
			LoadTokenWalletPort loadTokenWalletPort,
			TokenWalletConcurrencyPort concurrencyPort,
			TokenLedgerIdempotencySupport idempotencySupport
	) {
		this.loadTokenWalletPort = loadTokenWalletPort;
		this.concurrencyPort = concurrencyPort;
		this.idempotencySupport = idempotencySupport;
	}

	public TokenLedger execute(
			MemberUuid memberUuid,
			TransactionUuid idempotencyKey,
			Function<TokenWallet, TokenLedger> mutation
	) {
		return execute(memberUuid, idempotencyKey, mutation, ignored -> {
		});
	}

	public TokenLedger execute(
			MemberUuid memberUuid,
			TransactionUuid idempotencyKey,
			Function<TokenWallet, TokenLedger> mutation,
			Consumer<TokenLedger> afterNewSave
	) {
		IdempotencyKey key = IdempotencyKey.from(idempotencyKey);
		TokenLedger existing = idempotencySupport.findExisting(key);
		if (existing != null) {
			return existing;
		}
		return concurrencyPort.executeWithMemberLock(memberUuid, () -> {
			TokenLedger lockedExisting = idempotencySupport.findExisting(key);
			if (lockedExisting != null) {
				return lockedExisting;
			}
			TokenWallet wallet = loadTokenWalletPort.load(memberUuid);
			TokenLedger ledger = mutation.apply(wallet);
			TokenLedgerIdempotencySupport.SaveLedgerResult result = idempotencySupport.saveLedger(ledger);
			if (result.newlyCreated()) {
				afterNewSave.accept(result.ledger());
			}
			return result.ledger();
		});
	}
}
