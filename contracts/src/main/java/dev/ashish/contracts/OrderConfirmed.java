package dev.ashish.contracts;

import java.time.Instant;
import java.util.UUID;

public record OrderConfirmed(

		UUID eventId,
		UUID orderId,
		String customerId,
		String item,
		int quantity,
		Instant occurredAt

) implements OrderEvent {

	public static OrderConfirmed of(UUID orderId, String customerId, String item, int quantity) {
		return new OrderConfirmed(UUID.randomUUID(), orderId, customerId, item, quantity, Instant.now());
	}

}
