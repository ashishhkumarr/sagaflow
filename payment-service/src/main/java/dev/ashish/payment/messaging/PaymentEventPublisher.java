package dev.ashish.payment.messaging;

import dev.ashish.contracts.PaymentEvent;
import dev.ashish.contracts.Topics;
import dev.ashish.outbox.Outbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

	private final Outbox outbox;

	public PaymentEventPublisher(Outbox outbox) {
		this.outbox = outbox;
	}

	public void publish(PaymentEvent event) {
		// same row, same transaction as the payment itself. charging a card and then
		// losing the message would be the worst one to get wrong
		outbox.put(Topics.PAYMENT_EVENTS, event.orderId().toString(), event);
		log.info("queued {} for order {}", event.getClass().getSimpleName(), event.orderId());
	}

}
