package dev.ashish.contracts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentFailed(

		UUID eventId,
		UUID orderId,
		String customerId,
		BigDecimal amount,
		String reason,
		Instant occurredAt

) implements PaymentEvent {

	public static PaymentFailed of(UUID orderId, String customerId, BigDecimal amount, String reason) {
		return new PaymentFailed(UUID.randomUUID(), orderId, customerId, amount, reason, Instant.now());
	}

}
