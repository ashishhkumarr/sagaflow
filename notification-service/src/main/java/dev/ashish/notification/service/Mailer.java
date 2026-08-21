package dev.ashish.notification.service;

import dev.ashish.contracts.OrderCancelled;
import dev.ashish.contracts.OrderConfirmed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// nothing actually leaves the machine, it just writes what would have been sent
@Component
public class Mailer {

	private static final Logger log = LoggerFactory.getLogger(Mailer.class);

	public void orderConfirmed(OrderConfirmed event) {
		log.info("email to {}: your order for {} x{} is confirmed (order {})",
				event.customerId(), event.item(), event.quantity(), event.orderId());
	}

	public void orderCancelled(OrderCancelled event) {
		log.info("email to {}: sorry, your order for {} x{} could not go through - {} (order {})",
				event.customerId(), event.item(), event.quantity(), event.reason(), event.orderId());
	}

}
