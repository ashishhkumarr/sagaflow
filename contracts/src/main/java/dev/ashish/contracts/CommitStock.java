package dev.ashish.contracts;

import java.time.Instant;
import java.util.UUID;

public record CommitStock(

		UUID commandId,
		UUID orderId,
		Instant sentAt

) implements InventoryCommand {

	public static CommitStock of(UUID orderId) {
		return new CommitStock(UUID.randomUUID(), orderId, Instant.now());
	}

}
