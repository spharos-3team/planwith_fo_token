package com.planwith.planwith_fo_token.application.service.support;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_token.application.port.out.LoadTokenLedgerPort;
import com.planwith.planwith_fo_token.application.port.out.LoadTokenWalletPort;
import com.planwith.planwith_fo_token.application.port.out.SaveTokenWalletPort;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

@Component
public class TokenLedgerCommandExecutor {

	private final LoadTokenWalletPort loadTokenWalletPort;
	private final SaveTokenWalletPort saveTokenWalletPort;
	private final LoadTokenLedgerPort loadTokenLedgerPort;

	public TokenLedgerCommandExecutor(
			LoadTokenWalletPort loadTokenWalletPort,
			SaveTokenWalletPort saveTokenWalletPort,
			LoadTokenLedgerPort loadTokenLedgerPort
	) {
		this.loadTokenWalletPort = loadTokenWalletPort;
		this.saveTokenWalletPort = saveTokenWalletPort;
		this.loadTokenLedgerPort = loadTokenLedgerPort;
	}

	public Optional<TokenLedger> findProcessed(TransactionUuid transactionUuid) {
		return loadTokenLedgerPort.findByTransactionUuid(transactionUuid);
	}

	public TokenLedger executeMutation(
			TransactionUuid transactionUuid,
			MemberUuid memberUuid,
			Function<TokenWallet, TokenLedger> mutation
	) {
		Optional<TokenLedger> existing = loadTokenLedgerPort.findByTransactionUuid(transactionUuid);
		if (existing.isPresent()) {
			return existing.get();
		}
		TokenWallet wallet = loadTokenWalletPort.load(memberUuid);
		TokenLedger ledger = mutation.apply(wallet);
		return saveTokenWalletPort.saveMutation(ledger);
	}
}
