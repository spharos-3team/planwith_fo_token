package com.planwith.planwith_fo_token.application.service.support;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_token.application.command.GrantTokenCommand;
import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TransactionType;

@Component
public class TokenGrantExecutor {

	private final TokenWalletMutationExecutor mutationExecutor;

	public TokenGrantExecutor(TokenWalletMutationExecutor mutationExecutor) {
		this.mutationExecutor = mutationExecutor;
	}

	public TokenLedger grant(GrantTokenCommand command) {
		validate(command);
		return mutationExecutor.execute(
				command.memberUuid(),
				command.transactionUuid(),
				wallet -> wallet.grant(
						command.transactionUuid(),
						command.transactionType(),
						TokenCommandSupport.parseReferenceType(command.referenceType()),
						command.amount(),
						TokenCommandSupport.descriptionOrDefault(command.description(), defaultDescription(command)),
						Instant.now()
				)
		);
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
