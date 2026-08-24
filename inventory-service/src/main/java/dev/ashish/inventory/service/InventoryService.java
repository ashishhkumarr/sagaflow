package dev.ashish.inventory.service;

import dev.ashish.contracts.ReleaseStock;
import dev.ashish.contracts.ReserveStock;
import dev.ashish.contracts.StockRejected;
import dev.ashish.contracts.StockReleased;
import dev.ashish.contracts.StockReserved;
import dev.ashish.inventory.domain.Reservation;
import dev.ashish.inventory.domain.ReservationRepository;
import dev.ashish.inventory.domain.ReservationStatus;
import dev.ashish.inventory.domain.StockRepository;
import dev.ashish.inventory.messaging.InventoryEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
		// kafka delivers at least once so the same command can turn up twice. the
		// reservation is keyed on order id, row already there means it was done
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

	@Transactional
	public void release(ReleaseStock command) {
		Optional<Reservation> found = reservations.findById(command.orderId());
		if (found.isEmpty()) {
			log.warn("asked to release order {} but there is no reservation for it", command.orderId());
			return;
		}

		Reservation reservation = found.get();

		// releasing twice would put stock back that was never taken, so the row status
		// decides. the reply still goes out either way, otherwise a lost reply leaves
		// the order stuck waiting forever
		if (reservation.getStatus() == ReservationStatus.RELEASED) {
			log.info("order {} was already released, replying again", command.orderId());
		}
		else {
			stock.giveBack(reservation.getItem(), reservation.getQuantity());
			reservation.setStatus(ReservationStatus.RELEASED);
			log.info("gave back {} x{} from order {}", reservation.getItem(),
					reservation.getQuantity(), command.orderId());
		}

		publisher.publish(StockReleased.of(command.orderId(), reservation.getItem(),
				reservation.getQuantity()));
	}

}
