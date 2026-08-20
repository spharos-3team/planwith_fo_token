package com.planwith.planwith_fo_token.application.port.out;

import java.util.List;
import java.util.Optional;

import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntry;
import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntryType;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

public interface TokenLedgerPort {

	boolean existsByTransactionUuid(TransactionUuid transactionUuid);

	TokenLedgerEntry save(TokenLedgerEntry entry);

	Optional<TokenLedgerEntry> findByTransactionUuid(TransactionUuid transactionUuid);

	List<TokenLedgerEntry> findByMemberUuid(MemberUuid memberUuid, int page, int size);

	List<TokenLedgerEntry> findByMemberUuidAndEntryType(
			MemberUuid memberUuid,
			TokenLedgerEntryType entryType,
			int page,
			int size
	);
}
