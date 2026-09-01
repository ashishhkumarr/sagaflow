package dev.ashish.payment;

import dev.ashish.common.CorrelationConfig;
import dev.ashish.common.KafkaErrorConfig;
import dev.ashish.inbox.InboxConfig;
import dev.ashish.outbox.OutboxConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ CorrelationConfig.class, KafkaErrorConfig.class, OutboxConfig.class, InboxConfig.class })
public class PaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}

}
