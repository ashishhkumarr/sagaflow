package dev.ashish.inventory;

import dev.ashish.contracts.ReleaseStock;
import dev.ashish.contracts.ReserveStock;
import dev.ashish.contracts.Topics;
import dev.ashish.inventory.domain.ReservationRepository;
import dev.ashish.inventory.domain.ReservationStatus;
import dev.ashish.inventory.domain.Stock;
import dev.ashish.inventory.domain.StockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DuplicateDeliveryTest extends IntegrationTest {

	@Autowired
	KafkaTemplate<String, Object> kafka;

	@Autowired
	StockRepository stock;

	@Autowired
	ReservationRepository reservations;

	@Test
	void theSameCommandTwiceOnlyTakesStockOnce() {
		int before = available("red shoe");
		UUID orderId = UUID.randomUUID();
		ReserveStock command = ReserveStock.of(orderId, "red shoe", 1);

		try (EventStream events = new EventStream(KAFKA.getBootstrapServers(), Topics.INVENTORY_EVENTS)) {
			kafka.send(Topics.INVENTORY_COMMANDS, orderId.toString(), command);
			// exactly the same message again, id and all, the way kafka redelivers
			kafka.send(Topics.INVENTORY_COMMANDS, orderId.toString(), command);

			List<String> replies = events.collectFor(
					message -> message.contains(orderId.toString()) && message.contains("stock-reserved"),
					Duration.ofSeconds(12));

			// the second one still gets an answer, staying silent would leave the order
			// waiting for ever if the first answer went missing
			assertEquals(2, replies.size(), "expected a reply to both copies");
		}

		assertEquals(before - 1, available("red shoe"), "stock should only move once");
		assertEquals(1, reservations.findById(orderId).stream().count());
	}

	@Test
	void aResentCommandWithANewIdStillDoesNotTakeStockTwice() {
		int before = available("green hat");
		UUID orderId = UUID.randomUUID();

		kafka.send(Topics.INVENTORY_COMMANDS, orderId.toString(), ReserveStock.of(orderId, "green hat", 1));
		await().atMost(Duration.ofSeconds(20))
				.untilAsserted(() -> assertTrue(reservations.findById(orderId).isPresent()));

		// what the stuck order sweep does: same order, brand new command id, so the
		// inbox has never seen it
		kafka.send(Topics.INVENTORY_COMMANDS, orderId.toString(), ReserveStock.of(orderId, "green hat", 1));

		await().during(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(10))
				.untilAsserted(() -> assertEquals(before - 1, available("green hat")));
	}

	@Test
	void releasingTwiceOnlyGivesTheStockBackOnce() {
		int before = available("blue shirt");
		UUID orderId = UUID.randomUUID();

		kafka.send(Topics.INVENTORY_COMMANDS, orderId.toString(), ReserveStock.of(orderId, "blue shirt", 1));
		await().atMost(Duration.ofSeconds(20))
				.untilAsserted(() -> assertEquals(before - 1, available("blue shirt")));

		ReleaseStock release = new ReleaseStock(UUID.randomUUID(), orderId, Instant.now());
		kafka.send(Topics.INVENTORY_COMMANDS, orderId.toString(), release);
		kafka.send(Topics.INVENTORY_COMMANDS, orderId.toString(), release);

		// giving it back twice would invent a unit that never existed
		await().during(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(15))
				.untilAsserted(() -> assertEquals(before, available("blue shirt")));
		assertEquals(ReservationStatus.RELEASED, reservations.findById(orderId).orElseThrow().getStatus());
	}

	private int available(String item) {
		return stock.findById(item).map(Stock::getAvailable).orElseThrow();
	}

}
