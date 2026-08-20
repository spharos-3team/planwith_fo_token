package com.planwith.planwith_fo_token.application.service.support;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_token.application.port.out.LoadTokenLedgerPort;
import com.planwith.planwith_fo_token.application.port.out.SaveTokenWalletPort;
import com.planwith.planwith_fo_token.domain.exception.DuplicateTransactionException;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.vo.IdempotencyKey;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

@Component
public class TokenLedgerIdempotencySupport {

	private final LoadTokenLedgerPort loadTokenLedgerPort;
	private final SaveTokenWalletPort saveTokenWalletPort;

	public TokenLedgerIdempotencySupport(
			LoadTokenLedgerPort loadTokenLedgerPort,
			SaveTokenWalletPort saveTokenWalletPort
	) {
		this.loadTokenLedgerPort = loadTokenLedgerPort;
		this.saveTokenWalletPort = saveTokenWalletPort;
	}

	public TokenLedger findExisting(IdempotencyKey idempotencyKey) {
		return loadTokenLedgerPort.findByTransactionUuid(idempotencyKey.toTransactionUuid())
				.orElse(null);
	}

	public TokenLedger findExisting(TransactionUuid transactionUuid) {
		return findExisting(IdempotencyKey.from(transactionUuid));
	}

	public SaveLedgerResult saveLedger(TokenLedger ledger) {
		try {
			return new SaveLedgerResult(saveTokenWalletPort.saveMutation(ledger), true);
		} catch (DataIntegrityViolationException exception) {
			TokenLedger existing = loadTokenLedgerPort.findByTransactionUuid(ledger.transactionUuid())
					.orElseThrow(() -> new DuplicateTransactionException(
							"Duplicate idempotency key without existing ledger. key=" + ledger.transactionUuid()
					));
			return new SaveLedgerResult(existing, false);
		}
	}

	public record SaveLedgerResult(TokenLedger ledger, boolean newlyCreated) {
	}
}
