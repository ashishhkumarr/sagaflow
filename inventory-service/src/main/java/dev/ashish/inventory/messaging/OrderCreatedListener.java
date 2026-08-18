package dev.ashish.inventory.messaging;

import dev.ashish.contracts.OrderCreated;
import dev.ashish.contracts.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {

	private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);

	@KafkaListener(topics = Topics.ORDER_EVENTS)
	public void onOrderCreated(OrderCreated event) {
		log.info("heard about order {}: {} x{} for {}",
				event.orderId(), event.item(), event.quantity(), event.customerId());
	}

}
