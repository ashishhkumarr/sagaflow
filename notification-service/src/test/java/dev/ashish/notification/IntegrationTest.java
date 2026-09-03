package dev.ashish.notification;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.kafka.KafkaContainer;

// no database in this one, it only ever reads events and writes a log line
@SpringBootTest
public abstract class IntegrationTest {

	@ServiceConnection
	static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:4.0.0");

	static {
		KAFKA.start();
	}

}
