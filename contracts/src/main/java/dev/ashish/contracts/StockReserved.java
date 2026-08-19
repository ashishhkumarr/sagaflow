package dev.ashish.contracts;

import java.time.Instant;
import java.util.UUID;

public record StockReserved(

		UUID eventId,
		UUID orderId,
		String item,
		int quantity,
		Instant occurredAt

) implements InventoryEvent {

	public static StockReserved of(UUID orderId, String item, int quantity) {
		return new StockReserved(UUID.randomUUID(), orderId, item, quantity, Instant.now());
	}

}
