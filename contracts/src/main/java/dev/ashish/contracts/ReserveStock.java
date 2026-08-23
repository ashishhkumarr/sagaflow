package dev.ashish.contracts;

import java.time.Instant;
import java.util.UUID;

public record ReserveStock(

		UUID commandId,
		UUID orderId,
		String item,
		int quantity,
		Instant sentAt

) implements InventoryCommand {

	public static ReserveStock of(UUID orderId, String item, int quantity) {
		return new ReserveStock(UUID.randomUUID(), orderId, item, quantity, Instant.now());
	}

}
