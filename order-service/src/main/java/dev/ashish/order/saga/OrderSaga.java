package dev.ashish.order.saga;

import dev.ashish.contracts.CommitStock;
import dev.ashish.contracts.OrderCancelled;
import dev.ashish.contracts.OrderConfirmed;
import dev.ashish.contracts.ProcessPayment;
import dev.ashish.contracts.ReleaseStock;
import dev.ashish.contracts.ReserveStock;
import dev.ashish.order.domain.InvalidTransition;
import dev.ashish.order.domain.Order;
import dev.ashish.order.domain.OrderRepository;
import dev.ashish.order.domain.OrderStatus;
import dev.ashish.order.messaging.CommandPublisher;
import dev.ashish.order.messaging.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

// runs the order start to finish. the other services never talk to each other, they
// only answer commands from here
@Service
public class OrderSaga {

	private static final Logger log = LoggerFactory.getLogger(OrderSaga.class);

	private final OrderRepository orders;

	private final CommandPublisher commands;

	private final OrderEventPublisher events;

	public OrderSaga(OrderRepository orders, CommandPublisher commands, OrderEventPublisher events) {
		this.orders = orders;
		this.commands = commands;
		this.events = events;
	}

	@Transactional
	public void start(Order order) {
		order.moveTo(OrderStatus.AWAITING_STOCK);
		commands.reserveStock(ReserveStock.of(order.getId(), order.getItem(), order.getQuantity()));
	}

	@Transactional
	public void onStockReserved(UUID orderId) {
		step(orderId, order -> {
			order.moveTo(OrderStatus.AWAITING_PAYMENT);
			commands.processPayment(ProcessPayment.of(order.getId(), order.getCustomerId(), order.getAmount()));
		});
	}

	@Transactional
	public void onStockRejected(UUID orderId, String reason) {
		step(orderId, order -> cancel(order, reason));
	}

	@Transactional
	public void onPaymentSucceeded(UUID orderId) {
		step(orderId, order -> {
			order.moveTo(OrderStatus.CONFIRMED);
			commands.commitStock(CommitStock.of(order.getId()));
			events.publish(OrderConfirmed.of(order.getId(), order.getCustomerId(),
					order.getItem(), order.getQuantity()));
		});
	}

	@Transactional
	public void onPaymentFailed(UUID orderId, String reason) {
		step(orderId, order -> {
			order.startCompensating(reason);
			commands.releaseStock(ReleaseStock.of(order.getId()));
		});
	}

	@Transactional
	public void onStockReleased(UUID orderId) {
		step(orderId, order -> {
			// inventory can give the stock back on its own if a reservation sits too
			// long. there is nothing left to undo at that point, only to record it
			if (order.getStatus() == OrderStatus.AWAITING_PAYMENT) {
				order.startCompensating("stock reservation expired");
			}
			order.finishCompensating();
			events.publish(OrderCancelled.of(order.getId(), order.getCustomerId(),
					order.getItem(), order.getQuantity(), order.getCancelReason()));
		});
	}

	// the command is sent again rather than the order being cancelled. that is only safe
	// because the other services check for a repeat and answer with what they decided
	// the first time round
	@Transactional
	public void resend(Order order) {
		switch (order.getStatus()) {
			case AWAITING_STOCK -> commands.reserveStock(
					ReserveStock.of(order.getId(), order.getItem(), order.getQuantity()));
			case AWAITING_PAYMENT -> commands.processPayment(
					ProcessPayment.of(order.getId(), order.getCustomerId(), order.getAmount()));
			default -> {
				return;
			}
		}
		order.touch();
	}

	private void cancel(Order order, String reason) {
		order.cancel(reason);
		events.publish(OrderCancelled.of(order.getId(), order.getCustomerId(),
				order.getItem(), order.getQuantity(), reason));
	}

	// every reply lands here. a move the state machine refuses means it is a repeat or
	// a late reply from a step already past, so it gets dropped
	private void step(UUID orderId, Consumer<Order> change) {
		Optional<Order> found = orders.findById(orderId);
		if (found.isEmpty()) {
			log.warn("got a reply for order {} which is not in the db", orderId);
			return;
		}

		Order order = found.get();
		try {
			change.accept(order);
			log.info("order {} is now {}", orderId, order.getStatus());
		}
		catch (InvalidTransition e) {
			log.info("ignoring reply: {}", e.getMessage());
		}
	}

}
