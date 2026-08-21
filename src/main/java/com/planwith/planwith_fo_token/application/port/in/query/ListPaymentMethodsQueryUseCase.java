package com.planwith.planwith_fo_token.application.port.in.query;

import java.util.List;

import com.planwith.planwith_fo_token.application.query.ListPaymentMethodsQuery;
import com.planwith.planwith_fo_token.application.query.PaymentMethodResult;

public interface ListPaymentMethodsQueryUseCase {

	List<PaymentMethodResult> list(ListPaymentMethodsQuery query);
}
