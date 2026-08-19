package dev.ashish.contracts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreated(

		UUID eventId,
		UUID orderId,
		String customerId,
		String item,
		int quantity,
		BigDecimal amount,
		Instant occurredAt

) implements OrderEvent {

	public static OrderCreated of(UUID orderId, String customerId, String item, int quantity, BigDecimal amount) {
		return new OrderCreated(UUID.randomUUID(), orderId, customerId, item, quantity, amount, Instant.now());
	}

}
