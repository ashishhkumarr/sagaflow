package dev.ashish.contracts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentSucceeded(

		UUID eventId,
		UUID orderId,
		String customerId,
		BigDecimal amount,
		Instant occurredAt

) implements PaymentEvent {

	public static PaymentSucceeded of(UUID orderId, String customerId, BigDecimal amount) {
		return new PaymentSucceeded(UUID.randomUUID(), orderId, customerId, amount, Instant.now());
	}

}
