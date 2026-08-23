package dev.ashish.inventory.messaging;

import dev.ashish.contracts.InventoryCommand;
import dev.ashish.contracts.ReserveStock;
import dev.ashish.contracts.Topics;
import dev.ashish.inventory.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ReserveStockListener {

	private final InventoryService inventory;

	public ReserveStockListener(InventoryService inventory) {
		this.inventory = inventory;
	}

	@KafkaListener(topics = Topics.INVENTORY_COMMANDS)
	public void onCommand(InventoryCommand command) {
		switch (command) {
			case ReserveStock reserve -> inventory.reserve(reserve);
		}
	}

}
