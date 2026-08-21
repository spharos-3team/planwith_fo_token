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

	/**
	 * 카드 soft-delete 후 기본 결제수단 재지정.
	 * - 기본 카드 삭제 시 남은 ACTIVE 중 등록 시각이 가장 빠른 카드를 기본으로 승격
	 * - 남은 ACTIVE가 없으면 기본 카드 없음
	 */
	public static DeleteResult applyOnDelete(PaymentMethod target, List<PaymentMethod> activeMethods) {
		if (target.status() != PaymentMethodStatus.ACTIVE) {
			throw new InvalidPaymentMethodStateException(
					"Only ACTIVE payment methods can be deleted. paymentMethodUuid="
							+ target.paymentMethodUuid().value()
			);
		}

		boolean wasDefault = target.defaultMethod();
		PaymentMethod deleted = target.delete();
		List<PaymentMethod> remaining = activeMethods.stream()
				.filter(method -> !method.paymentMethodUuid().equals(target.paymentMethodUuid()))
				.toList();

		PaymentMethod promotedDefault = null;
		List<PaymentMethod> updatedRemaining = remaining;
		if (wasDefault && !remaining.isEmpty()) {
			PaymentMethod nextDefault = remaining.get(0).markAsDefault();
			promotedDefault = nextDefault;
			updatedRemaining = remaining.stream()
					.map(method -> method.paymentMethodUuid().equals(nextDefault.paymentMethodUuid())
							? nextDefault
							: method)
					.toList();
		}

		return new DeleteResult(deleted, promotedDefault, updatedRemaining);
	}

	public record DefaultChangeResult(List<PaymentMethod> updatedMethods) {
	}

	public record DeleteResult(
			PaymentMethod deleted,
			PaymentMethod promotedDefault,
			List<PaymentMethod> remainingActive
	) {
	}
}
