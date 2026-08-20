package com.planwith.planwith_fo_token.adapter.out.persistence.wallet;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(
		name = "token_wallet",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_token_wallet_member_uuid",
				columnNames = {"member_uuid"}
		)
)
class TokenWalletJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "wallet_id")
	private Long walletId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Column(name = "balance", nullable = false)
	private long balance;

	@Version
	@Column(name = "version", nullable = false)
	private long version;

	protected TokenWalletJpaEntity() {
	}

	static TokenWalletJpaEntity create(UUID memberUuid) {
		TokenWalletJpaEntity entity = new TokenWalletJpaEntity();
		entity.memberUuid = memberUuid;
		entity.balance = 0L;
		entity.version = 0L;
		return entity;
	}

	Long getWalletId() {
		return walletId;
	}

	UUID getMemberUuid() {
		return memberUuid;
	}

	long getBalance() {
		return balance;
	}

	long getVersion() {
		return version;
	}

	void setBalance(long balance) {
		this.balance = balance;
	}
}
