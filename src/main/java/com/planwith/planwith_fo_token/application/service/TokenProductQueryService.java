package com.planwith.planwith_fo_token.application.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_token.application.port.in.query.ListTokenProductsQueryUseCase;
import com.planwith.planwith_fo_token.application.query.TokenProductResult;
import com.planwith.planwith_fo_token.domain.model.TokenProduct;
import com.planwith.planwith_fo_token.domain.service.TokenProductPolicy;

@Service
public class TokenProductQueryService implements ListTokenProductsQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(TokenProductQueryService.class);

	@Override
	public List<TokenProductResult> listProducts() {
		log.info("TokenProductQueryService : listProducts : 토큰 상품 목록 조회");
		List<TokenProductResult> results = TokenProductPolicy.listAll().stream()
				.map(TokenProductQueryService::toResult)
				.toList();
		log.info("TokenProductQueryService : listProducts : 토큰 상품 목록 조회 완료 - count={}", results.size());
		return results;
	}

	private static TokenProductResult toResult(TokenProduct product) {
		return new TokenProductResult(
				product.code(),
				product.name(),
				product.salePrice(),
				product.baseTokenAmount(),
				product.bonusTokenAmount(),
				product.totalTokenAmount()
		);
	}
}
