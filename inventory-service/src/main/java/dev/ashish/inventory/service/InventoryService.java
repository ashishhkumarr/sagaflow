package dev.ashish.inventory.service;

import dev.ashish.contracts.ReserveStock;
import dev.ashish.contracts.StockRejected;
import dev.ashish.contracts.StockReserved;
import dev.ashish.inventory.domain.Reservation;
import dev.ashish.inventory.domain.ReservationRepository;
import dev.ashish.inventory.domain.StockRepository;
import dev.ashish.inventory.messaging.InventoryEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

	private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

	private final StockRepository stock;

	private final ReservationRepository reservations;

	private final InventoryEventPublisher publisher;

	public InventoryService(StockRepository stock, ReservationRepository reservations,
			InventoryEventPublisher publisher) {
		this.stock = stock;
		this.reservations = reservations;
		this.publisher = publisher;
	}

	@Transactional
	public void reserve(ReserveStock command) {
		// kafka can hand us the same message twice, the reservation row is keyed on the
		// order id so if it is already there we have done this one before
		if (reservations.existsById(command.orderId())) {
			log.info("order {} already reserved, skipping", command.orderId());
			return;
		}

		int updated = stock.reserveIfAvailable(command.item(), command.quantity());
		if (updated == 0) {
			String reason = stock.existsById(command.item()) ? "not enough stock" : "unknown item";
			log.info("rejecting order {}: {}", command.orderId(), reason);
			publisher.publish(StockRejected.of(command.orderId(), command.item(), command.quantity(), reason));
			return;
		}

		reservations.save(new Reservation(command.orderId(), command.item(), command.quantity()));
		log.info("reserved {} x{} for order {}", command.item(), command.quantity(), command.orderId());
		publisher.publish(StockReserved.of(command.orderId(), command.item(), command.quantity()));
	}

}
