package dev.ashish.payment.config;

import dev.ashish.contracts.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

	@Bean
	NewTopic paymentEvents() {
		return TopicBuilder.name(Topics.PAYMENT_EVENTS)
				.partitions(3)
				.replicas(1)
				.build();
	}

}
