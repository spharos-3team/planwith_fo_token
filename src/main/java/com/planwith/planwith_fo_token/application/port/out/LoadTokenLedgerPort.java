package com.planwith.planwith_fo_token.application.port.out;

import java.util.List;
import java.util.Optional;

import com.planwith.planwith_fo_token.domain.model.TokenLedger;
import com.planwith.planwith_fo_token.domain.model.TransactionType;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;

public interface LoadTokenLedgerPort {

	boolean existsByTransactionUuid(TransactionUuid transactionUuid);

	Optional<TokenLedger> findByTransactionUuid(TransactionUuid transactionUuid);

	List<TokenLedger> findByMemberUuidChronological(MemberUuid memberUuid);

	List<TokenLedger> findByMemberUuid(MemberUuid memberUuid, int page, int size);

	List<TokenLedger> findByMemberUuidAndEntryType(
			MemberUuid memberUuid,
			TransactionType transactionType,
			int page,
			int size
	);
}
