package com.planwith.planwith_fo_token.adapter.out.persistence.wallet;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "token_wallet_state")
class TokenWalletStateJpaEntity {

	@Id
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Column(name = "version", nullable = false)
	private long version;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected TokenWalletStateJpaEntity() {
	}

	static TokenWalletStateJpaEntity create(UUID memberUuid, Instant updatedAt) {
		TokenWalletStateJpaEntity entity = new TokenWalletStateJpaEntity();
		entity.memberUuid = memberUuid;
		entity.version = 0L;
		entity.updatedAt = updatedAt;
		return entity;
	}

	void touch(Instant updatedAt) {
		this.updatedAt = updatedAt;
		this.version++;
	}

	UUID getMemberUuid() {
		return memberUuid;
	}

	long getVersion() {
		return version;
	}
}
