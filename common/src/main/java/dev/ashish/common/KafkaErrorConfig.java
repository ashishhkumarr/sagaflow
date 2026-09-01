package dev.ashish.common;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class KafkaErrorConfig {

	private static final String DEAD_LETTER_SUFFIX = ".dlt";

	@Bean
	DefaultErrorHandler kafkaErrorHandler(KafkaProperties properties) {
		// a few goes with a growing gap first. a database hiccup fixes itself in a
		// second or two, and treating that like a broken message would throw away work
		// that was only ever going to fail once
		ExponentialBackOff backOff = new ExponentialBackOff(500L, 2.0);
		backOff.setMaxAttempts(4);

		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
				deadLetterTemplate(properties),
				(record, exception) -> new TopicPartition(record.topic() + DEAD_LETTER_SUFFIX, -1));

		return new DefaultErrorHandler(recoverer, backOff);
	}

	// built here rather than exposed as a bean, otherwise boot sees a KafkaTemplate
	// already exists and stops making the normal one the services rely on
	private KafkaTemplate<Object, Object> deadLetterTemplate(KafkaProperties properties) {
		Map<Class<?>, Serializer<?>> byType = new LinkedHashMap<>();
		// a message that failed to parse is still just bytes, anything else has already
		// been turned into an object and has to go back out as json
		byType.put(byte[].class, new ByteArraySerializer());
		byType.put(String.class, new StringSerializer());
		byType.put(Object.class, new JacksonJsonSerializer<>());

		DelegatingByTypeSerializer serializer = new DelegatingByTypeSerializer(byType, true);
		DefaultKafkaProducerFactory<Object, Object> factory = new DefaultKafkaProducerFactory<>(
				properties.buildProducerProperties(), serializer, serializer);

		return new KafkaTemplate<>(factory);
	}

}
