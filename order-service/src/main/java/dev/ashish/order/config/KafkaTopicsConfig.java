package dev.ashish.order.config;

import dev.ashish.contracts.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

	// auto create would leave the partition count up to the broker default
	@Bean
	NewTopic orderEvents() {
		return TopicBuilder.name(Topics.ORDER_EVENTS)
				.partitions(3)
				.replicas(1)
				.build();
	}

	@Bean
	NewTopic inventoryCommands() {
		return TopicBuilder.name(Topics.INVENTORY_COMMANDS)
				.partitions(3)
				.replicas(1)
				.build();
	}

	@Bean
	NewTopic paymentCommands() {
		return TopicBuilder.name(Topics.PAYMENT_COMMANDS)
				.partitions(3)
				.replicas(1)
				.build();
	}

}
