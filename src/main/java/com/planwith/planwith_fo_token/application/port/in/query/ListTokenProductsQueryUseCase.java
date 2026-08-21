package com.planwith.planwith_fo_token.application.port.in.query;

import java.util.List;

import com.planwith.planwith_fo_token.application.query.TokenProductResult;

public interface ListTokenProductsQueryUseCase {

	List<TokenProductResult> listProducts();
}
