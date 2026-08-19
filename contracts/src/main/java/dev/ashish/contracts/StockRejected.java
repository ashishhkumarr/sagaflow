package dev.ashish.contracts;

import java.time.Instant;
import java.util.UUID;

public record StockRejected(

		UUID eventId,
		UUID orderId,
		String item,
		int quantity,
		String reason,
		Instant occurredAt

) implements InventoryEvent {

	public static StockRejected of(UUID orderId, String item, int quantity, String reason) {
		return new StockRejected(UUID.randomUUID(), orderId, item, quantity, reason, Instant.now());
	}

}
