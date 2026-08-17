package dev.ashish.order.config;

import dev.ashish.contracts.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

	// kafka would auto create this but then the partition count is whatever the broker
	// defaults to, so declaring it here keeps it the same everywhere
	@Bean
	NewTopic orderEvents() {
		return TopicBuilder.name(Topics.ORDER_EVENTS)
				.partitions(3)
				.replicas(1)
				.build();
	}

}
