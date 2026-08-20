package com.planwith.planwith_fo_token.adapter.out.persistence.ledger;

import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TokenType;
import com.planwith.planwith_fo_token.domain.model.TransactionType;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;
import com.planwith.planwith_fo_token.domain.service.TokenPolicy;

final class TokenLedgerPersistenceMapper {

	private TokenLedgerPersistenceMapper() {
	}

	static TokenLedger toDomain(TokenLedgerJpaEntity entity) {
		return TokenLedger.restore(
				entity.getTokenLedgerId(),
				new TransactionUuid(entity.getTokenLedgerUuid()),
				new MemberUuid(entity.getMemberUuid()),
				entity.getTransactionType(),
				deriveTokenType(entity),
				entity.getAmount(),
				entity.getBalanceAfter(),
				entity.getReferenceType(),
				entity.getDescription(),
				entity.getOccurredAt(),
				entity.getCreatedAt()
		);
	}

	static TokenLedgerJpaEntity toEntity(TokenLedger ledger) {
		return TokenLedgerJpaEntity.create(
				ledger.tokenLedgerUuid().value(),
				ledger.memberUuid().value(),
				ledger.transactionType(),
				ledger.amount(),
				ledger.balanceAfter(),
				ledger.referenceType(),
				ledger.description(),
				ledger.occurredAt(),
				ledger.createdAt()
		);
	}

	private static TokenType deriveTokenType(TokenLedgerJpaEntity entity) {
		if (entity.getTransactionType() == TransactionType.EXPIRE) {
			return TokenType.FREE;
		}
		if (entity.getTransactionType() == TransactionType.CHARGE
				|| entity.getTransactionType() == TransactionType.REWARD) {
			return TokenPolicy.tokenTypeOfGrant(entity.getTransactionType(), entity.getReferenceType());
		}
		return null;
	}
}
