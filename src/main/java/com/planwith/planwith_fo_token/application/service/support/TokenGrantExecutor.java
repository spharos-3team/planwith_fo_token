package com.planwith.planwith_fo_token.application.service.support;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;
import com.planwith.planwith_fo_token.application.port.out.LoadTokenLedgerPort;
import com.planwith.planwith_fo_token.application.port.out.LoadTokenWalletPort;
import com.planwith.planwith_fo_token.application.port.out.SaveTokenWalletPort;
import com.planwith.planwith_fo_token.application.port.out.TokenEventOutboxPort;
import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TokenWallet;
import com.planwith.planwith_fo_token.domain.model.TransactionType;

@Component
public class TokenGrantExecutor {

	private final LoadTokenWalletPort loadTokenWalletPort;
	private final SaveTokenWalletPort saveTokenWalletPort;
	private final LoadTokenLedgerPort loadTokenLedgerPort;
	private final TokenEventOutboxPort tokenEventOutboxPort;

	public TokenGrantExecutor(
			LoadTokenWalletPort loadTokenWalletPort,
			SaveTokenWalletPort saveTokenWalletPort,
			LoadTokenLedgerPort loadTokenLedgerPort,
			TokenEventOutboxPort tokenEventOutboxPort
	) {
		this.loadTokenWalletPort = loadTokenWalletPort;
		this.saveTokenWalletPort = saveTokenWalletPort;
		this.loadTokenLedgerPort = loadTokenLedgerPort;
		this.tokenEventOutboxPort = tokenEventOutboxPort;
	}

	public TokenLedger grant(GrantTokenCommand command) {
		validate(command);
		return loadTokenLedgerPort.findByTransactionUuid(command.transactionUuid())
				.orElseGet(() -> executeNewGrant(command));
	}

	private TokenLedger executeNewGrant(GrantTokenCommand command) {
		ReferenceType referenceType = TokenCommandSupport.parseReferenceType(command.referenceType());
		TokenWallet wallet = loadTokenWalletPort.load(command.memberUuid());
		TokenLedger ledger = wallet.grant(
				command.transactionUuid(),
				command.transactionType(),
				referenceType,
				command.amount(),
				TokenCommandSupport.descriptionOrDefault(command.description(), defaultDescription(command)),
				Instant.now()
		);
		TokenLedger saved = saveTokenWalletPort.saveMutation(ledger);
		TokenGrantOutboxSupport.saveGrantOutbox(tokenEventOutboxPort, saved);
		return saved;
	}

	private static void validate(GrantTokenCommand command) {
		if (command.amount() <= 0) {
			throw new IllegalArgumentException("Grant amount must be positive.");
		}
		if (command.transactionType() != TransactionType.CHARGE
				&& command.transactionType() != TransactionType.REWARD) {
			throw new IllegalArgumentException("Grant supports only CHARGE or REWARD transaction types.");
		}
	}

	private static String defaultDescription(GrantTokenCommand command) {
		if (command.transactionType() == TransactionType.CHARGE) {
			return "Token charge grant";
		}
		if (ReferenceType.GRADE_REWARD.name().equals(command.referenceType())) {
			return "Grade reward grant";
		}
		return "Token bonus grant";
	}
}
