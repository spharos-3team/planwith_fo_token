package com.planwith.planwith_fo_token.application.service.support;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_token.application.command.UseTokenCommand;
import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TokenLedger;

@Component
public class TokenUseExecutor {

	private static final Set<ReferenceType> USE_REFERENCE_TYPES = EnumSet.of(
			ReferenceType.AI_SCHEDULE,
			ReferenceType.IMPORT_SCHEDULE,
			ReferenceType.PDF_DOWNLOAD
	);

	private final TokenWalletMutationExecutor mutationExecutor;

	public TokenUseExecutor(TokenWalletMutationExecutor mutationExecutor) {
		this.mutationExecutor = mutationExecutor;
	}

	public TokenLedger use(UseTokenCommand command) {
		validate(command);
		return mutationExecutor.execute(
				command.memberUuid(),
				command.transactionUuid(),
				wallet -> wallet.use(
						command.transactionUuid(),
						command.amount(),
						TokenCommandSupport.parseReferenceType(command.referenceType()),
						TokenCommandSupport.descriptionOrDefault(command.description(), "Token use"),
						Instant.now()
				)
		);
	}

	private static void validate(UseTokenCommand command) {
		if (command.amount() <= 0) {
			throw new IllegalArgumentException("Use amount must be positive.");
		}
		ReferenceType referenceType = TokenCommandSupport.parseReferenceType(command.referenceType());
		if (referenceType == null || !USE_REFERENCE_TYPES.contains(referenceType)) {
			throw new IllegalArgumentException(
					"Use referenceType must be one of AI_SCHEDULE, IMPORT_SCHEDULE, PDF_DOWNLOAD."
			);
		}
	}
}
