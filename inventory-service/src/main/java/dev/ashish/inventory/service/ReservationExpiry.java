package dev.ashish.inventory.service;

import dev.ashish.contracts.StockReleased;
import dev.ashish.inventory.domain.Reservation;
import dev.ashish.inventory.domain.ReservationRepository;
import dev.ashish.inventory.domain.ReservationStatus;
import dev.ashish.inventory.domain.StockRepository;
import dev.ashish.inventory.messaging.InventoryEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

// last resort. everything else only recovers because the message was still sitting on
// the topic, this covers the case where the answer really is never coming and the stock
// would otherwise be held for nobody
@Component
public class ReservationExpiry {

	private static final Logger log = LoggerFactory.getLogger(ReservationExpiry.class);

	private final ReservationRepository reservations;

	private final StockRepository stock;

	private final InventoryEventPublisher publisher;

	private final Duration ttl;

	public ReservationExpiry(ReservationRepository reservations, StockRepository stock,
			InventoryEventPublisher publisher, @Value("${inventory.reservation-ttl}") Duration ttl) {
		this.reservations = reservations;
		this.stock = stock;
		this.publisher = publisher;
		this.ttl = ttl;
	}

	@Scheduled(fixedDelayString = "${inventory.expiry-sweep-ms}")
	@Transactional
	public void releaseExpired() {
		List<Reservation> held = reservations.findHeldSince(Instant.now().minus(ttl));

		for (Reservation reservation : held) {
			stock.giveBack(reservation.getItem(), reservation.getQuantity());
			reservation.setStatus(ReservationStatus.RELEASED);
			log.warn("reservation for order {} sat unresolved for over {}, giving back {} x{}",
					reservation.getOrderId(), ttl, reservation.getItem(), reservation.getQuantity());
			publisher.publish(StockReleased.of(reservation.getOrderId(), reservation.getItem(),
					reservation.getQuantity()));
		}
	}

}
