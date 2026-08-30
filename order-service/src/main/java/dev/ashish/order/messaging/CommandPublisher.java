package dev.ashish.order.messaging;

import dev.ashish.contracts.InventoryCommand;
import dev.ashish.contracts.PaymentCommand;
import dev.ashish.contracts.Topics;
import dev.ashish.outbox.Outbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CommandPublisher {

	private static final Logger log = LoggerFactory.getLogger(CommandPublisher.class);

	private final Outbox outbox;

	public CommandPublisher(Outbox outbox) {
		this.outbox = outbox;
	}

	public void reserveStock(InventoryCommand command) {
		queue(Topics.INVENTORY_COMMANDS, command.orderId().toString(), command);
	}

	public void releaseStock(InventoryCommand command) {
		queue(Topics.INVENTORY_COMMANDS, command.orderId().toString(), command);
	}

	public void commitStock(InventoryCommand command) {
		queue(Topics.INVENTORY_COMMANDS, command.orderId().toString(), command);
	}

	public void processPayment(PaymentCommand command) {
		queue(Topics.PAYMENT_COMMANDS, command.orderId().toString(), command);
	}

	private void queue(String topic, String key, Object command) {
		outbox.put(topic, key, command);
		log.info("queued {} for order {}", command.getClass().getSimpleName(), key);
	}

}
