package dev.ashish.inventory;

import dev.ashish.common.CorrelationConfig;
import dev.ashish.outbox.OutboxConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ CorrelationConfig.class, OutboxConfig.class })
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

}
