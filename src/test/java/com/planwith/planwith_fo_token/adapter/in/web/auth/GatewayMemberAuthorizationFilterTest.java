package com.planwith.planwith_fo_token.adapter.in.web.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class GatewayMemberAuthorizationFilterTest {

	private static final UUID MEMBER_UUID = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");

	private final GatewayMemberAuthorizationFilter filter = new GatewayMemberAuthorizationFilter(true);

	@Test
	void allowsAuthenticatedMemberToAccessOwnTokenResource() throws Exception {
		MockHttpServletRequest request = memberRequest(MEMBER_UUID);
		request.addHeader(GatewayMemberAuthorizationFilter.AUTHENTICATED_MEMBER_HEADER, MEMBER_UUID);
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertThat(chain.getRequest()).isNotNull();
		assertThat(response.getStatus()).isEqualTo(200);
	}

	@Test
	void rejectsRequestWithoutAuthenticatedMemberHeader() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(memberRequest(MEMBER_UUID), response, new MockFilterChain());

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentAsString()).contains("AUTHENTICATION_REQUIRED");
	}

	@Test
	void rejectsAccessToAnotherMembersTokenResource() throws Exception {
		MockHttpServletRequest request = memberRequest(MEMBER_UUID);
		request.addHeader(
				GatewayMemberAuthorizationFilter.AUTHENTICATED_MEMBER_HEADER,
				UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb")
		);
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getStatus()).isEqualTo(403);
		assertThat(response.getContentAsString()).contains("TOKEN_MEMBER_ACCESS_DENIED");
	}

	private static MockHttpServletRequest memberRequest(UUID memberUuid) {
		return new MockHttpServletRequest(
				"GET",
				"/api/planwith-fo-token/members/" + memberUuid + "/tokens/balance"
		);
	}
}
