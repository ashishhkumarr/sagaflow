package dev.ashish.payment.messaging;

import dev.ashish.contracts.PaymentEvent;
import dev.ashish.contracts.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

	private final KafkaTemplate<String, Object> kafka;

	public PaymentEventPublisher(KafkaTemplate<String, Object> kafka) {
		this.kafka = kafka;
	}

	public void publish(PaymentEvent event) {
		kafka.send(Topics.PAYMENT_EVENTS, event.orderId().toString(), event);
		log.info("published {} for order {}", event.getClass().getSimpleName(), event.orderId());
	}

}
