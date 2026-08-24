package dev.ashish.contracts;

import java.time.Instant;
import java.util.UUID;

public record ReleaseStock(

		UUID commandId,
		UUID orderId,
		Instant sentAt

) implements InventoryCommand {

	public static ReleaseStock of(UUID orderId) {
		return new ReleaseStock(UUID.randomUUID(), orderId, Instant.now());
	}

}
