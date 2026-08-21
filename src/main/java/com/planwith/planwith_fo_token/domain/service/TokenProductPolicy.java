package com.planwith.planwith_fo_token.domain.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.planwith.planwith_fo_token.domain.exception.TokenProductNotFoundException;
import com.planwith.planwith_fo_token.domain.model.TokenProduct;
import com.planwith.planwith_fo_token.domain.model.TokenProductCode;

/**
 * 토큰 상품 가격·지급량 정책. 클라이언트 금액이 아닌 서버 정책을 사용한다.
 */
public final class TokenProductPolicy {

	private static final Map<TokenProductCode, TokenProduct> PRODUCTS = new EnumMap<>(TokenProductCode.class);

	static {
		PRODUCTS.put(TokenProductCode.TRIAL, new TokenProduct(TokenProductCode.TRIAL, "체험팩", 1_000L, 10L, 0L));
		PRODUCTS.put(TokenProductCode.BASIC, new TokenProduct(TokenProductCode.BASIC, "기본팩", 4_900L, 55L, 5L));
		PRODUCTS.put(TokenProductCode.POPULAR, new TokenProduct(TokenProductCode.POPULAR, "인기팩", 9_900L, 120L, 20L));
		PRODUCTS.put(TokenProductCode.LARGE, new TokenProduct(TokenProductCode.LARGE, "대용량팩", 19_900L, 260L, 60L));
	}

	private TokenProductPolicy() {
	}

	public static List<TokenProduct> listAll() {
		return List.copyOf(PRODUCTS.values());
	}

	public static TokenProduct require(TokenProductCode code) {
		TokenProduct product = PRODUCTS.get(code);
		if (product == null) {
			throw new TokenProductNotFoundException("Unknown token product. code=" + code);
		}
		return product;
	}

	public static TokenProduct require(String code) {
		if (code == null || code.isBlank()) {
			throw new TokenProductNotFoundException("Token product code is required.");
		}
		try {
			return require(TokenProductCode.valueOf(code.trim().toUpperCase()));
		} catch (IllegalArgumentException exception) {
			throw new TokenProductNotFoundException("Unknown token product. code=" + code);
		}
	}
}
