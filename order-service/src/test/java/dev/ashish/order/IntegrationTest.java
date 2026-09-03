package dev.ashish.order;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

// real postgres and real kafka in throwaway containers. mocks would happily let a
// broken serializer or a missing migration through, which is most of what has actually
// gone wrong while building this
@SpringBootTest
public abstract class IntegrationTest {

	// started once for the whole run instead of per test class, otherwise every class
	// pays the container startup cost again
	@ServiceConnection
	static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16");

	@ServiceConnection
	static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:4.0.0");

	static {
		POSTGRES.start();
		KAFKA.start();
	}

	@DynamicPropertySource
	static void fastPolling(DynamicPropertyRegistry registry) {
		// the outbox poller and the sweeps run far more often here so a test does not
		// sit around waiting for the production timings
		registry.add("outbox.poll-ms", () -> 200);
		registry.add("saga.sweep-ms", () -> 200);
		registry.add("saga.retry-after", () -> "1h");
	}

}
