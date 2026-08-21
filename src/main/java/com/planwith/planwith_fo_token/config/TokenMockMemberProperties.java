package com.planwith.planwith_fo_token.config;

import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "token.mock")
public class TokenMockMemberProperties {

	static final UUID DEFAULT_MEMBER_UUID = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");

	private boolean enabled = false;
	private UUID memberUuid = DEFAULT_MEMBER_UUID;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public UUID getMemberUuid() {
		return memberUuid;
	}

	public void setMemberUuid(UUID memberUuid) {
		this.memberUuid = memberUuid;
	}

	public UUID resolvedMemberUuid() {
		return memberUuid == null ? DEFAULT_MEMBER_UUID : memberUuid;
	}
}
