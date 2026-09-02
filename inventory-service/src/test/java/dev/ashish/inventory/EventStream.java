package dev.ashish.inventory;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

// reads a topic straight from the broker so a test can check what actually went out on
// the wire, not just what the service thinks it sent
final class EventStream implements AutoCloseable {

	private final KafkaConsumer<String, String> consumer;

	EventStream(String bootstrapServers, String topic) {
		Map<String, Object> config = new HashMap<>();
		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		config.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID());
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		this.consumer = new KafkaConsumer<>(config, new StringDeserializer(), new StringDeserializer());
		this.consumer.subscribe(List.of(topic));
	}

	String awaitMessage(Predicate<String> matches, Duration timeout) {
		Instant giveUpAt = Instant.now().plus(timeout);
		while (Instant.now().isBefore(giveUpAt)) {
			ConsumerRecords<String, String> batch = consumer.poll(Duration.ofMillis(500));
			for (ConsumerRecord<String, String> record : batch) {
				if (matches.test(record.value())) {
					return record.value();
				}
			}
		}
		throw new AssertionError("no matching message on the topic within " + timeout);
	}

	@Override
	public void close() {
		consumer.close();
	}

}
