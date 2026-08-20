package dev.ashish.contracts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockReserved(

		UUID eventId,
		UUID orderId,
		String customerId,
		String item,
		int quantity,
		BigDecimal amount,
		Instant occurredAt

) implements InventoryEvent {

	public static StockReserved of(UUID orderId, String customerId, String item, int quantity, BigDecimal amount) {
		return new StockReserved(UUID.randomUUID(), orderId, customerId, item, quantity, amount, Instant.now());
	}

}
