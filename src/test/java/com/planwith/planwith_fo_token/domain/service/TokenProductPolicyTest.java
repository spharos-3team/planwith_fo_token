package com.planwith.planwith_fo_token.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_token.domain.exception.TokenProductNotFoundException;
import com.planwith.planwith_fo_token.domain.model.TokenProduct;
import com.planwith.planwith_fo_token.domain.model.TokenProductCode;

class TokenProductPolicyTest {

	@Test
	void listsFourServerControlledProducts() {
		assertThat(TokenProductPolicy.listAll()).hasSize(4);
		assertThat(TokenProductPolicy.require(TokenProductCode.TRIAL).totalTokenAmount()).isEqualTo(10L);
		assertThat(TokenProductPolicy.require(TokenProductCode.BASIC).totalTokenAmount()).isEqualTo(60L);
		assertThat(TokenProductPolicy.require(TokenProductCode.POPULAR).totalTokenAmount()).isEqualTo(140L);
		assertThat(TokenProductPolicy.require(TokenProductCode.LARGE).totalTokenAmount()).isEqualTo(320L);
	}

	@Test
	void basicPackUsesServerPriceAndBonus() {
		TokenProduct basic = TokenProductPolicy.require("BASIC");
		assertThat(basic.name()).isEqualTo("기본팩");
		assertThat(basic.salePrice()).isEqualTo(4_900L);
		assertThat(basic.baseTokenAmount()).isEqualTo(55L);
		assertThat(basic.bonusTokenAmount()).isEqualTo(5L);
	}

	@Test
	void unknownProductFails() {
		assertThatThrownBy(() -> TokenProductPolicy.require("UNKNOWN"))
				.isInstanceOf(TokenProductNotFoundException.class);
	}
}
