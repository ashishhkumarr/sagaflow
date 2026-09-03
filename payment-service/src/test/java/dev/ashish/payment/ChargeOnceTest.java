package dev.ashish.payment;

import dev.ashish.contracts.ProcessPayment;
import dev.ashish.contracts.Topics;
import dev.ashish.payment.domain.Payment;
import dev.ashish.payment.domain.PaymentRepository;
import dev.ashish.payment.domain.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChargeOnceTest extends IntegrationTest {

	@Autowired
	KafkaTemplate<String, Object> kafka;

	@Autowired
	PaymentRepository payments;

	@Test
	void takesTheMoneyAndSaysSo() {
		UUID orderId = UUID.randomUUID();

		try (EventStream events = new EventStream(KAFKA.getBootstrapServers(), Topics.PAYMENT_EVENTS)) {
			kafka.send(Topics.PAYMENT_COMMANDS, orderId.toString(),
					ProcessPayment.of(orderId, "cust-1", new BigDecimal("40.00")));

			String reply = events.awaitMessage(
					message -> message.contains(orderId.toString()) && message.contains("payment-succeeded"),
					Duration.ofSeconds(30));
			assertTrue(reply.contains("40.0"), reply);
		}

		Payment payment = payments.findById(orderId).orElseThrow();
		assertEquals(PaymentStatus.CHARGED, payment.getStatus());
	}

	@Test
	void theSameCommandTwiceOnlyChargesOnce() {
		UUID orderId = UUID.randomUUID();
		ProcessPayment command = ProcessPayment.of(orderId, "cust-2", new BigDecimal("75.00"));

		try (EventStream events = new EventStream(KAFKA.getBootstrapServers(), Topics.PAYMENT_EVENTS)) {
			kafka.send(Topics.PAYMENT_COMMANDS, orderId.toString(), command);
			kafka.send(Topics.PAYMENT_COMMANDS, orderId.toString(), command);

			List<String> replies = events.collectFor(
					message -> message.contains(orderId.toString()) && message.contains("payment-succeeded"),
					Duration.ofSeconds(12));
			assertEquals(2, replies.size(), "both copies should be answered");
		}

		// the money is what matters here, one row and one charge
		assertEquals(1, payments.findById(orderId).stream().count());
		assertEquals(0, new BigDecimal("75.00").compareTo(payments.findById(orderId).orElseThrow().getAmount()));
	}

	@Test
	void declinesAndSaysWhy() {
		UUID orderId = UUID.randomUUID();

		try (EventStream events = new EventStream(KAFKA.getBootstrapServers(), Topics.PAYMENT_EVENTS)) {
			// anything over the configured limit gets turned down
			kafka.send(Topics.PAYMENT_COMMANDS, orderId.toString(),
					ProcessPayment.of(orderId, "cust-3", new BigDecimal("900.00")));

			String reply = events.awaitMessage(
					message -> message.contains(orderId.toString()) && message.contains("payment-failed"),
					Duration.ofSeconds(30));
			assertTrue(reply.contains("amount over the limit"), reply);
		}

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
				assertEquals(PaymentStatus.DECLINED, payments.findById(orderId).orElseThrow().getStatus()));
	}

}
