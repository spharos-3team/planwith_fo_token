package com.planwith.planwith_fo_token.application.service.support;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_token.application.port.out.LoadTokenWalletPort;
import com.planwith.planwith_fo_token.application.port.out.TokenEventOutboxPort;
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
	private final TokenEventOutboxPort tokenEventOutboxPort;

	public TokenWalletMutationExecutor(
			LoadTokenWalletPort loadTokenWalletPort,
			TokenWalletConcurrencyPort concurrencyPort,
			TokenLedgerIdempotencySupport idempotencySupport,
			TokenEventOutboxPort tokenEventOutboxPort
	) {
		this.loadTokenWalletPort = loadTokenWalletPort;
		this.concurrencyPort = concurrencyPort;
		this.idempotencySupport = idempotencySupport;
		this.tokenEventOutboxPort = tokenEventOutboxPort;
	}

	public TokenLedger execute(
			MemberUuid memberUuid,
			TransactionUuid idempotencyKey,
			Function<TokenWallet, TokenLedger> mutation
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
				tokenEventOutboxPort.save(TokenMutationOutboxSupport.toOutboxMessage(result.ledger()));
			}
			return result.ledger();
		});
	}
}
