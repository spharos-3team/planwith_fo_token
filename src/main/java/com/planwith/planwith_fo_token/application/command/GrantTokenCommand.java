package com.planwith.planwith_fo_token.application.command;

import com.planwith.planwith_fo_token.domain.model.ReferenceType;
import com.planwith.planwith_fo_token.domain.model.TransactionType;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

public record GrantTokenCommand(
		TransactionUuid transactionUuid,
		MemberUuid memberUuid,
		TransactionType transactionType,
		long amount,
		String referenceType,
		String referenceUuid,
		String description
) {

	public static GrantTokenCommand paidCharge(
			TransactionUuid transactionUuid,
			MemberUuid memberUuid,
			long amount,
			String paymentReference,
			String description
	) {
		return new GrantTokenCommand(
				transactionUuid,
				memberUuid,
				TransactionType.CHARGE,
				amount,
				ReferenceType.PAYMENT.name(),
				paymentReference,
				description
		);
	}

	public static GrantTokenCommand gradeReward(
			TransactionUuid transactionUuid,
			MemberUuid memberUuid,
			long amount,
			String rewardType,
			String description
	) {
		return new GrantTokenCommand(
				transactionUuid,
				memberUuid,
				TransactionType.REWARD,
				amount,
				ReferenceType.GRADE_REWARD.name(),
				rewardType,
				description
		);
	}

	public static GrantTokenCommand bonusReward(
			TransactionUuid transactionUuid,
			MemberUuid memberUuid,
			long amount,
			String referenceUuid,
			String description
	) {
		return new GrantTokenCommand(
				transactionUuid,
				memberUuid,
				TransactionType.REWARD,
				amount,
				null,
				referenceUuid,
				description
		);
	}

	public static GrantTokenCommand recoveryCharge(
			TransactionUuid transactionUuid,
			MemberUuid memberUuid,
			long amount,
			String paymentReference,
			String description
	) {
		return paidCharge(transactionUuid, memberUuid, amount, paymentReference, description);
	}

	public static GrantTokenCommand fromCharge(ChargeTokenCommand command) {
		return paidCharge(
				command.transactionUuid(),
				command.memberUuid(),
				command.amount(),
				command.referenceUuid(),
				command.description()
		);
	}

	public static GrantTokenCommand fromRecover(RecoverTokenCommand command) {
		return recoveryCharge(
				command.transactionUuid(),
				command.memberUuid(),
				command.amount(),
				command.referenceUuid(),
				command.description()
		);
	}
}
