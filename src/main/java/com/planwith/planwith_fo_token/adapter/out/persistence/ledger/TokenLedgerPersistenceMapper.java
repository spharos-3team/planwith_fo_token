package com.planwith.planwith_fo_token.adapter.out.persistence.ledger;

import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntry;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

final class TokenLedgerPersistenceMapper {

	private TokenLedgerPersistenceMapper() {
	}

	static TokenLedgerEntry toDomain(TokenLedgerJpaEntity entity) {
		return TokenLedgerEntry.pending(
				entity.getLedgerId(),
				new TransactionUuid(entity.getTransactionUuid()),
				new MemberUuid(entity.getMemberUuid()),
				entity.getEntryType(),
				entity.getAmount(),
				entity.getBalanceAfter(),
				entity.getReferenceType(),
				entity.getReferenceUuid(),
				entity.getOccurredAt()
		);
	}

	static TokenLedgerJpaEntity toEntity(TokenLedgerEntry entry) {
		return TokenLedgerJpaEntity.create(
				entry.transactionUuid().value(),
				entry.memberUuid().value(),
				entry.entryType(),
				entry.amount(),
				entry.balanceAfter(),
				entry.referenceType(),
				entry.referenceUuid(),
				entry.occurredAt()
		);
	}
}
