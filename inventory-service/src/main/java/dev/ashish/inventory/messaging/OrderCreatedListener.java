package dev.ashish.inventory.messaging;

import dev.ashish.contracts.OrderCreated;
import dev.ashish.contracts.OrderEvent;
import dev.ashish.contracts.Topics;
import dev.ashish.inventory.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {

	private final InventoryService inventory;

	public OrderCreatedListener(InventoryService inventory) {
		this.inventory = inventory;
	}

	@KafkaListener(topics = Topics.ORDER_EVENTS)
	public void onOrderEvent(OrderEvent event) {
		if (event instanceof OrderCreated created) {
			inventory.reserve(created);
		}
	}

}
