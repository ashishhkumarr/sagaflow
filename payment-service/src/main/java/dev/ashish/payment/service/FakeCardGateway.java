package dev.ashish.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// stands in for a real card processor. rules are predictable on purpose so a test
// can decide up front whether a charge fails
@Component
public class FakeCardGateway {

	private final BigDecimal declineOver;

	public FakeCardGateway(@Value("${payment.decline-over}") BigDecimal declineOver) {
		this.declineOver = declineOver;
	}

	public ChargeResult charge(String customerId, BigDecimal amount) {
		if (customerId != null && customerId.startsWith("fail-")) {
			return ChargeResult.declined("card reported stolen");
		}
		if (amount.compareTo(declineOver) > 0) {
			return ChargeResult.declined("amount over the limit");
		}
		return ChargeResult.ok();
	}

	public record ChargeResult(boolean approved, String reason) {

		static ChargeResult ok() {
			return new ChargeResult(true, null);
		}

		static ChargeResult declined(String reason) {
			return new ChargeResult(false, reason);
		}

	}

}
