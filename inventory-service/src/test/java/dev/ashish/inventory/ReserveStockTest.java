package dev.ashish.inventory;

import dev.ashish.contracts.ReserveStock;
import dev.ashish.contracts.Topics;
import dev.ashish.inventory.domain.Reservation;
import dev.ashish.inventory.domain.ReservationRepository;
import dev.ashish.inventory.domain.ReservationStatus;
import dev.ashish.inventory.domain.Stock;
import dev.ashish.inventory.domain.StockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReserveStockTest extends IntegrationTest {

	@Autowired
	KafkaTemplate<String, Object> kafka;

	@Autowired
	StockRepository stock;

	@Autowired
	ReservationRepository reservations;

	@Test
	void takesTheStockAndSaysSo() {
		int before = available("red shoe");
		UUID orderId = UUID.randomUUID();

		try (EventStream events = new EventStream(KAFKA.getBootstrapServers(), Topics.INVENTORY_EVENTS)) {
			kafka.send(Topics.INVENTORY_COMMANDS, orderId.toString(),
					ReserveStock.of(orderId, "red shoe", 2));

			String reply = events.awaitMessage(
					message -> message.contains(orderId.toString()) && message.contains("stock-reserved"),
					Duration.ofSeconds(30));
			assertTrue(reply.contains("\"quantity\":2"), reply);
		}

		Reservation reservation = reservations.findById(orderId).orElseThrow();
		assertEquals(ReservationStatus.RESERVED, reservation.getStatus());
		assertEquals(2, reservation.getQuantity());
		assertEquals(before - 2, available("red shoe"));
	}

	@Test
	void refusesWhenThereIsNotEnough() {
		int before = available("black jacket");
		UUID orderId = UUID.randomUUID();

		try (EventStream events = new EventStream(KAFKA.getBootstrapServers(), Topics.INVENTORY_EVENTS)) {
			// only one black jacket was ever seeded
			kafka.send(Topics.INVENTORY_COMMANDS, orderId.toString(),
					ReserveStock.of(orderId, "black jacket", 5));

			String reply = events.awaitMessage(
					message -> message.contains(orderId.toString()) && message.contains("stock-rejected"),
					Duration.ofSeconds(30));
			assertTrue(reply.contains("not enough stock"), reply);
		}

		await().atMost(Duration.ofSeconds(5))
				.untilAsserted(() -> assertEquals(before, available("black jacket")));
		assertTrue(reservations.findById(orderId).isEmpty());
	}

	private int available(String item) {
		return stock.findById(item).map(Stock::getAvailable).orElseThrow();
	}

}
