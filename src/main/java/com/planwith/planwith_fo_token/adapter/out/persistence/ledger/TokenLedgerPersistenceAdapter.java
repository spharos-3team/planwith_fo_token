package com.planwith.planwith_fo_token.adapter.out.persistence.ledger;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_token.application.port.out.TokenLedgerPort;
import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntry;
import com.planwith.planwith_fo_token.domain.model.TokenLedgerEntryType;
import com.planwith.planwith_fo_token.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_token.domain.model.vo.TransactionUuid;
import com.planwith.planwith_fo_token.domain.service.TokenPolicy;

@Component
public class TokenLedgerPersistenceAdapter implements TokenLedgerPort {

	private static final Logger log = LoggerFactory.getLogger(TokenLedgerPersistenceAdapter.class);

	private final SpringDataTokenLedgerRepository repository;

	public TokenLedgerPersistenceAdapter(SpringDataTokenLedgerRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByTransactionUuid(TransactionUuid transactionUuid) {
		return repository.existsByTokenLedgerUuid(transactionUuid.value());
	}

	@Override
	@Transactional
	public TokenLedgerEntry save(TokenLedgerEntry entry) {
		if (entry.tokenLedgerId() != null && !TokenPolicy.ledgerMutable()) {
			throw new IllegalStateException("Ledger UPDATE/DELETE is not allowed.");
		}
		TokenLedgerJpaEntity saved = repository.save(TokenLedgerPersistenceMapper.toEntity(entry));
		log.debug("TokenLedgerPersistenceAdapter : save : 토큰 원장 INSERT - tokenLedgerUuid={}, type={}",
				entry.tokenLedgerUuid(), entry.transactionType());
		return TokenLedgerPersistenceMapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<TokenLedgerEntry> findByTransactionUuid(TransactionUuid transactionUuid) {
		return repository.findByTokenLedgerUuid(transactionUuid.value())
				.map(TokenLedgerPersistenceMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TokenLedgerEntry> findByMemberUuidChronological(MemberUuid memberUuid) {
		return repository.findByMemberUuidOrderByOccurredAtAsc(memberUuid.value())
				.stream()
				.map(TokenLedgerPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<TokenLedgerEntry> findByMemberUuid(MemberUuid memberUuid, int page, int size) {
		return repository.findByMemberUuidOrderByOccurredAtDesc(
						memberUuid.value(),
						PageRequest.of(normalizePage(page), normalizeSize(size))
				)
				.stream()
				.map(TokenLedgerPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<TokenLedgerEntry> findByMemberUuidAndEntryType(
			MemberUuid memberUuid,
			TokenLedgerEntryType entryType,
			int page,
			int size
	) {
		return repository.findByMemberUuidAndTransactionTypeOrderByOccurredAtDesc(
						memberUuid.value(),
						entryType,
						PageRequest.of(normalizePage(page), normalizeSize(size))
				)
				.stream()
				.map(TokenLedgerPersistenceMapper::toDomain)
				.toList();
	}

	private static int normalizePage(int page) {
		return Math.max(page, 0);
	}

	private static int normalizeSize(int size) {
		return size > 0 ? size : 20;
	}
}
