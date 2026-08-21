package dev.ashish.order.messaging;

import dev.ashish.contracts.InventoryEvent;
import dev.ashish.contracts.StockRejected;
import dev.ashish.contracts.Topics;
import dev.ashish.order.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

	private final OrderService orderService;

	public InventoryEventListener(OrderService orderService) {
		this.orderService = orderService;
	}

	// this service reads two topics that carry different event types, so the type has
	// to be set per listener instead of once in application.yml
	@KafkaListener(topics = Topics.INVENTORY_EVENTS,
			properties = "spring.json.value.default.type=dev.ashish.contracts.InventoryEvent")
	public void onInventoryEvent(InventoryEvent event) {
		// a reservation going through is not the end of the story, we wait for payment
		if (event instanceof StockRejected rejected) {
			orderService.cancel(rejected.orderId(), rejected.reason());
		}
	}

}
