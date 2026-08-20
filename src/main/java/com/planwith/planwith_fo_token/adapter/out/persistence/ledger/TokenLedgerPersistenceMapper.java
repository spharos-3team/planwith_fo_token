package com.planwith.planwith_fo_token.adapter.out.persistence.ledger;

import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntry;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

final class TokenLedgerPersistenceMapper {

	private TokenLedgerPersistenceMapper() {
	}

	static TokenLedgerEntry toDomain(TokenLedgerJpaEntity entity) {
		return TokenLedgerEntry.restore(
				entity.getTokenLedgerId(),
				new TransactionUuid(entity.getTokenLedgerUuid()),
				new MemberUuid(entity.getMemberUuid()),
				entity.getTransactionType(),
				entity.getAmount(),
				entity.getBalanceAfter(),
				entity.getReferenceType(),
				entity.getDescription(),
				entity.getOccurredAt(),
				entity.getCreatedAt()
		);
	}

	static TokenLedgerJpaEntity toEntity(TokenLedgerEntry entry) {
		return TokenLedgerJpaEntity.create(
				entry.tokenLedgerUuid().value(),
				entry.memberUuid().value(),
				entry.transactionType(),
				entry.amount(),
				entry.balanceAfter(),
				entry.referenceType(),
				entry.description(),
				entry.occurredAt(),
				entry.createdAt()
		);
	}
}
