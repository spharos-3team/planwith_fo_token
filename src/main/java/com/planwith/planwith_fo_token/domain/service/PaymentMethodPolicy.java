package com.planwith.planwith_fo_token.domain.service;

import java.util.ArrayList;
import java.util.List;

import com.planwith.planwith_fo_token.domain.exception.InvalidPaymentMethodStateException;
import com.planwith.planwith_fo_token.domain.model.PaymentMethod;
import com.planwith.planwith_fo_token.domain.model.PaymentMethodStatus;
import com.planwith.planwith_fo_token.domain.model.vo.PaymentMethodUuid;

/**
 * 결제수단 Default 및 Status 정책.
 */
public final class PaymentMethodPolicy {

	private PaymentMethodPolicy() {
	}

	public static boolean canBeDefault(PaymentMethod paymentMethod) {
		return paymentMethod.status() == PaymentMethodStatus.ACTIVE;
	}

	public static boolean shouldRegisterAsDefault(List<PaymentMethod> activeMethods, boolean requestedDefault) {
		return requestedDefault || activeMethods.isEmpty();
	}

	public static DefaultChangeResult applyDefaultChange(
			List<PaymentMethod> activeMethods,
			PaymentMethodUuid targetPaymentMethodUuid
	) {
		List<PaymentMethod> updated = new ArrayList<>();
		boolean foundTarget = false;

		for (PaymentMethod method : activeMethods) {
			if (method.paymentMethodUuid().equals(targetPaymentMethodUuid)) {
				if (!canBeDefault(method)) {
					throw new InvalidPaymentMethodStateException(
							"Only ACTIVE payment methods can be default. paymentMethodUuid="
									+ targetPaymentMethodUuid.value()
					);
				}
				updated.add(method.markAsDefault());
				foundTarget = true;
				continue;
			}
			if (method.defaultMethod()) {
				updated.add(method.clearDefault());
				continue;
			}
			updated.add(method);
		}

		if (!foundTarget) {
			throw new InvalidPaymentMethodStateException(
					"Default target payment method not found. paymentMethodUuid=" + targetPaymentMethodUuid.value()
			);
		}
		return new DefaultChangeResult(updated);
	}

	public static List<PaymentMethod> clearExistingDefaults(List<PaymentMethod> activeMethods) {
		return activeMethods.stream()
				.map(method -> method.defaultMethod() ? method.clearDefault() : method)
				.toList();
	}

	public record DefaultChangeResult(List<PaymentMethod> updatedMethods) {
	}
}
