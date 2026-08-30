package dev.ashish.inventory.service;

import dev.ashish.contracts.CommitStock;
import dev.ashish.contracts.ReleaseStock;
import dev.ashish.contracts.ReserveStock;
import dev.ashish.contracts.StockRejected;
import dev.ashish.contracts.StockReleased;
import dev.ashish.contracts.StockReserved;
import dev.ashish.inbox.Inbox;
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

	private final Inbox inbox;

	public InventoryService(StockRepository stock, ReservationRepository reservations,
			InventoryEventPublisher publisher, Inbox inbox) {
		this.stock = stock;
		this.reservations = reservations;
		this.publisher = publisher;
		this.inbox = inbox;
	}

	@Transactional
	public void reserve(ReserveStock command) {
		// kafka delivers at least once so the same command can turn up twice
		if (inbox.alreadyHandled(command.commandId())) {
			log.info("already handled the reserve for order {}, saying the same thing again",
					command.orderId());
			replyAgain(command);
			return;
		}
		inbox.markHandled(command.commandId());

		// a retry arrives as a new command with a new id, so the inbox will not know it.
		// the reservation keyed on order id is what stops the stock going down twice
		if (reservations.existsById(command.orderId())) {
			log.info("order {} already has a reservation, replying again", command.orderId());
			replyAgain(command);
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

	// staying quiet on a repeat is only safe if the first answer got through. if it did
	// not, the order sits waiting forever, so the same answer goes out again
	private void replyAgain(ReserveStock command) {
		boolean held = reservations.findById(command.orderId())
				.filter(r -> r.getStatus() == ReservationStatus.RESERVED)
				.isPresent();

		if (held) {
			publisher.publish(StockReserved.of(command.orderId(), command.item(), command.quantity()));
		}
		else {
			String reason = stock.existsById(command.item()) ? "not enough stock" : "unknown item";
			publisher.publish(StockRejected.of(command.orderId(), command.item(), command.quantity(), reason));
		}
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

	// the order finished, so this stock is genuinely sold. without this the reservation
	// stays RESERVED for ever and the expiry sweep eventually hands it back
	@Transactional
	public void commit(CommitStock command) {
		reservations.findById(command.orderId())
				.filter(r -> r.getStatus() == ReservationStatus.RESERVED)
				.ifPresent(r -> {
					r.setStatus(ReservationStatus.COMMITTED);
					log.info("order {} completed, {} x{} is sold", command.orderId(), r.getItem(), r.getQuantity());
				});
	}

}
