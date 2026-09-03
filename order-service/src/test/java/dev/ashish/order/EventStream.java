package dev.ashish.order;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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

	// keeps reading for the whole window instead of stopping at the first hit, so a
	// test can tell one reply apart from two
	List<String> collectFor(Predicate<String> matches, Duration window) {
		List<String> found = new ArrayList<>();
		Instant stopAt = Instant.now().plus(window);
		while (Instant.now().isBefore(stopAt)) {
			ConsumerRecords<String, String> batch = consumer.poll(Duration.ofMillis(300));
			for (ConsumerRecord<String, String> record : batch) {
				if (matches.test(record.value())) {
					found.add(record.value());
				}
			}
		}
		return found;
	}

	@Override
	public void close() {
		consumer.close();
	}

}
