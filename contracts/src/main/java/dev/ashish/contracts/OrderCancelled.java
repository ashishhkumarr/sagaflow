package dev.ashish.contracts;

import java.time.Instant;
import java.util.UUID;

public record OrderCancelled(

		UUID eventId,
		UUID orderId,
		String customerId,
		String item,
		int quantity,
		String reason,
		Instant occurredAt

) implements OrderEvent {

	public static OrderCancelled of(UUID orderId, String customerId, String item, int quantity, String reason) {
		return new OrderCancelled(UUID.randomUUID(), orderId, customerId, item, quantity, reason, Instant.now());
	}

}
