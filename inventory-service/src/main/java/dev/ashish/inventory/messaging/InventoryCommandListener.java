package dev.ashish.inventory.messaging;

import dev.ashish.contracts.CommitStock;
import dev.ashish.contracts.InventoryCommand;
import dev.ashish.contracts.ReleaseStock;
import dev.ashish.contracts.ReserveStock;
import dev.ashish.contracts.Topics;
import dev.ashish.inventory.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryCommandListener {

	private final InventoryService inventory;

	public InventoryCommandListener(InventoryService inventory) {
		this.inventory = inventory;
	}

	@KafkaListener(topics = Topics.INVENTORY_COMMANDS)
	public void onCommand(InventoryCommand command) {
		switch (command) {
			case ReserveStock reserve -> inventory.reserve(reserve);
			case ReleaseStock release -> inventory.release(release);
			case CommitStock commit -> inventory.commit(commit);
		}
	}

}
