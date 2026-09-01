package dev.ashish.order;

import dev.ashish.common.CorrelationConfig;
import dev.ashish.common.KafkaErrorConfig;
import dev.ashish.outbox.OutboxConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ CorrelationConfig.class, KafkaErrorConfig.class, OutboxConfig.class })
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}
