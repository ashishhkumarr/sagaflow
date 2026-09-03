package dev.ashish.order;

import dev.ashish.contracts.PaymentFailed;
import dev.ashish.contracts.PaymentSucceeded;
import dev.ashish.contracts.StockReleased;
import dev.ashish.contracts.StockReserved;
import dev.ashish.contracts.Topics;
import dev.ashish.order.domain.Order;
import dev.ashish.order.domain.OrderRepository;
import dev.ashish.order.domain.OrderStatus;
import dev.ashish.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// the other services are not running here, so their replies are put on the topics by
// hand. that is the point, it tests the saga on its own rather than everything at once
class SagaFlowTest extends IntegrationTest {

	@Autowired
	KafkaTemplate<String, Object> kafka;

	@Autowired
	OrderService orders;

	@Autowired
	OrderRepository repository;

	@Test
	void goesAllTheWayToConfirmed() {
		Order order = orders.placeOrder("cust-1", "red shoe", 1, new BigDecimal("30.00"));
		UUID id = order.getId();

		awaitStatus(id, OrderStatus.AWAITING_STOCK);
		expectCommand(Topics.INVENTORY_COMMANDS, id, "reserve-stock");

		kafka.send(Topics.INVENTORY_EVENTS, id.toString(), StockReserved.of(id, "red shoe", 1));
		awaitStatus(id, OrderStatus.AWAITING_PAYMENT);
		expectCommand(Topics.PAYMENT_COMMANDS, id, "process-payment");

		kafka.send(Topics.PAYMENT_EVENTS, id.toString(),
				PaymentSucceeded.of(id, "cust-1", new BigDecimal("30.00")));
		awaitStatus(id, OrderStatus.CONFIRMED);

		// the stock has to be marked sold, otherwise the expiry sweep hands it back later
		expectCommand(Topics.INVENTORY_COMMANDS, id, "commit-stock");
	}

	@Test
	void aFailedPaymentGivesTheStockBackBeforeCancelling() {
		Order order = orders.placeOrder("cust-2", "green hat", 2, new BigDecimal("40.00"));
		UUID id = order.getId();

		awaitStatus(id, OrderStatus.AWAITING_STOCK);
		kafka.send(Topics.INVENTORY_EVENTS, id.toString(), StockReserved.of(id, "green hat", 2));
		awaitStatus(id, OrderStatus.AWAITING_PAYMENT);

		kafka.send(Topics.PAYMENT_EVENTS, id.toString(),
				PaymentFailed.of(id, "cust-2", new BigDecimal("40.00"), "card reported stolen"));

		// not cancelled yet, the stock is still out there
		awaitStatus(id, OrderStatus.COMPENSATING);
		expectCommand(Topics.INVENTORY_COMMANDS, id, "release-stock");

		kafka.send(Topics.INVENTORY_EVENTS, id.toString(), StockReleased.of(id, "green hat", 2));
		awaitStatus(id, OrderStatus.CANCELLED);
		assertEquals("card reported stolen", repository.findById(id).orElseThrow().getCancelReason());
	}

	@Test
	void aReplyForAStepAlreadyPastIsIgnored() {
		Order order = orders.placeOrder("cust-3", "red shoe", 1, new BigDecimal("20.00"));
		UUID id = order.getId();
		awaitStatus(id, OrderStatus.AWAITING_STOCK);

		// payment could never have run yet, the stock was never reserved
		kafka.send(Topics.PAYMENT_EVENTS, id.toString(),
				PaymentSucceeded.of(id, "cust-3", new BigDecimal("20.00")));

		await().during(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(10))
				.untilAsserted(() -> assertEquals(OrderStatus.AWAITING_STOCK, statusOf(id)));
	}

	private void expectCommand(String topic, UUID orderId, String type) {
		try (EventStream commands = new EventStream(KAFKA.getBootstrapServers(), topic)) {
			String sent = commands.awaitMessage(
					message -> message.contains(orderId.toString()) && message.contains(type),
					Duration.ofSeconds(20));
			assertTrue(sent.contains(type), sent);
		}
	}

	private void awaitStatus(UUID orderId, OrderStatus expected) {
		await().atMost(Duration.ofSeconds(20))
				.untilAsserted(() -> assertEquals(expected, statusOf(orderId)));
	}

	private OrderStatus statusOf(UUID orderId) {
		return repository.findById(orderId).orElseThrow().getStatus();
	}

}
