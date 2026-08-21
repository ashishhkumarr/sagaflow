package dev.ashish.order.messaging;

import dev.ashish.contracts.OrderEvent;
import dev.ashish.contracts.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

	private final KafkaTemplate<String, Object> kafka;

	public OrderEventPublisher(KafkaTemplate<String, Object> kafka) {
		this.kafka = kafka;
	}

	public void publish(OrderEvent event) {
		// keyed on the order id so all the events for one order go to the same
		// partition, otherwise they can get processed out of order
		kafka.send(Topics.ORDER_EVENTS, event.orderId().toString(), event);
		log.info("published {} for order {}", event.getClass().getSimpleName(), event.orderId());
	}

}
