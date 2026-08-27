package dev.ashish.inventory.messaging;

import dev.ashish.contracts.InventoryEvent;
import dev.ashish.contracts.Topics;
import dev.ashish.outbox.Outbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(InventoryEventPublisher.class);

	private final Outbox outbox;

	public InventoryEventPublisher(Outbox outbox) {
		this.outbox = outbox;
	}

	public void publish(InventoryEvent event) {
		// goes in the same transaction as the stock change, so a crash here cannot
		// leave stock taken with nobody told about it
		outbox.put(Topics.INVENTORY_EVENTS, event.orderId().toString(), event);
		log.info("queued {} for order {}", event.getClass().getSimpleName(), event.orderId());
	}

}
