package dev.ashish.order.messaging;

import dev.ashish.contracts.InventoryCommand;
import dev.ashish.contracts.PaymentCommand;
import dev.ashish.contracts.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CommandPublisher {

	private static final Logger log = LoggerFactory.getLogger(CommandPublisher.class);

	private final KafkaTemplate<String, Object> kafka;

	public CommandPublisher(KafkaTemplate<String, Object> kafka) {
		this.kafka = kafka;
	}

	public void reserveStock(InventoryCommand command) {
		send(Topics.INVENTORY_COMMANDS, command.orderId().toString(), command);
	}

	public void releaseStock(InventoryCommand command) {
		send(Topics.INVENTORY_COMMANDS, command.orderId().toString(), command);
	}

	public void processPayment(PaymentCommand command) {
		send(Topics.PAYMENT_COMMANDS, command.orderId().toString(), command);
	}

	private void send(String topic, String key, Object command) {
		kafka.send(topic, key, command);
		log.info("sent {} for order {}", command.getClass().getSimpleName(), key);
	}

}
