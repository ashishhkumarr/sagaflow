package dev.ashish.contracts;

import java.time.Instant;
import java.util.UUID;

public record StockReleased(

		UUID eventId,
		UUID orderId,
		String item,
		int quantity,
		Instant occurredAt

) implements InventoryEvent {

	public static StockReleased of(UUID orderId, String item, int quantity) {
		return new StockReleased(UUID.randomUUID(), orderId, item, quantity, Instant.now());
	}

}
