package com.planwith.planwith_fo_token.adapter.out.persistence.processed;

import java.time.Instant;
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

@Entity
@Table(
		name = "processed_token_event",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_processed_token_event_uuid",
				columnNames = {"event_uuid"}
		)
)
class ProcessedTokenEventJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "processed_id")
	private Long processedId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "event_uuid", nullable = false, length = 36)
	private UUID eventUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Column(name = "event_type", nullable = false, length = 50)
	private String eventType;

	@Column(name = "processed_at", nullable = false)
	private Instant processedAt;

	protected ProcessedTokenEventJpaEntity() {
	}

	static ProcessedTokenEventJpaEntity create(
			UUID eventUuid,
			UUID memberUuid,
			String eventType,
			Instant processedAt
	) {
		ProcessedTokenEventJpaEntity entity = new ProcessedTokenEventJpaEntity();
		entity.eventUuid = eventUuid;
		entity.memberUuid = memberUuid;
		entity.eventType = eventType;
		entity.processedAt = processedAt;
		return entity;
	}
}
