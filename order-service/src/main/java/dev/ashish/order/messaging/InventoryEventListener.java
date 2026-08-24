package dev.ashish.order.messaging;

import dev.ashish.contracts.InventoryEvent;
import dev.ashish.contracts.StockRejected;
import dev.ashish.contracts.StockReleased;
import dev.ashish.contracts.StockReserved;
import dev.ashish.contracts.Topics;
import dev.ashish.order.saga.OrderSaga;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

	private final OrderSaga saga;

	public InventoryEventListener(OrderSaga saga) {
		this.saga = saga;
	}

	// this service reads two topics that carry different event types, so the type has
	// to be set per listener instead of once in application.yml
	@KafkaListener(topics = Topics.INVENTORY_EVENTS,
			properties = "spring.json.value.default.type=dev.ashish.contracts.InventoryEvent")
	public void onInventoryEvent(InventoryEvent event) {
		switch (event) {
			case StockReserved reserved -> saga.onStockReserved(reserved.orderId());
			case StockRejected rejected -> saga.onStockRejected(rejected.orderId(), rejected.reason());
			case StockReleased released -> saga.onStockReleased(released.orderId());
		}
	}

}
