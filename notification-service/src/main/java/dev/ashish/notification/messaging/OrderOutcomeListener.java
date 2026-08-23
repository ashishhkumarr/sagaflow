package dev.ashish.notification.messaging;

import dev.ashish.contracts.OrderCancelled;
import dev.ashish.contracts.OrderConfirmed;
import dev.ashish.contracts.OrderEvent;
import dev.ashish.contracts.Topics;
import dev.ashish.notification.service.Mailer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderOutcomeListener {

	private final Mailer mailer;

	public OrderOutcomeListener(Mailer mailer) {
		this.mailer = mailer;
	}

	@KafkaListener(topics = Topics.ORDER_EVENTS)
	public void onOrderEvent(OrderEvent event) {
		// order created is on this topic too but there is nothing to email about yet
		if (event instanceof OrderConfirmed confirmed) {
			mailer.orderConfirmed(confirmed);
		}
		else if (event instanceof OrderCancelled cancelled) {
			mailer.orderCancelled(cancelled);
		}
	}

}
