package dev.ashish.inventory.messaging;

import dev.ashish.contracts.InventoryEvent;
import dev.ashish.contracts.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(InventoryEventPublisher.class);

	private final KafkaTemplate<String, Object> kafka;

	public InventoryEventPublisher(KafkaTemplate<String, Object> kafka) {
		this.kafka = kafka;
	}

	public void publish(InventoryEvent event) {
		kafka.send(Topics.INVENTORY_EVENTS, event.orderId().toString(), event);
		log.info("published {} for order {}", event.getClass().getSimpleName(), event.orderId());
	}

}
