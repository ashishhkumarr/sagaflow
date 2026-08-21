package dev.ashish.order.service;

import dev.ashish.contracts.OrderCancelled;
import dev.ashish.contracts.OrderConfirmed;
import dev.ashish.contracts.OrderCreated;
import dev.ashish.order.domain.Order;
import dev.ashish.order.domain.OrderRepository;
import dev.ashish.order.domain.OrderStatus;
import dev.ashish.order.messaging.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

	private static final Logger log = LoggerFactory.getLogger(OrderService.class);

	private final OrderRepository orders;

	private final OrderEventPublisher publisher;

	public OrderService(OrderRepository orders, OrderEventPublisher publisher) {
		this.orders = orders;
		this.publisher = publisher;
	}

	@Transactional
	public Order placeOrder(String customerId, String item, int quantity, BigDecimal amount) {
		Order order = orders.save(new Order(customerId, item, quantity, amount));
		log.info("saved order {} for customer {}", order.getId(), customerId);

		publisher.publish(OrderCreated.of(order.getId(), order.getCustomerId(),
				order.getItem(), order.getQuantity(), order.getAmount()));

		return order;
	}

	@Transactional
	public void confirm(UUID orderId) {
		finish(orderId, OrderStatus.CONFIRMED, null);
	}

	@Transactional
	public void cancel(UUID orderId, String reason) {
		finish(orderId, OrderStatus.CANCELLED, reason);
	}

	private void finish(UUID orderId, OrderStatus status, String reason) {
		Optional<Order> found = orders.findById(orderId);
		if (found.isEmpty()) {
			log.warn("got a reply for order {} which is not in our db", orderId);
			return;
		}

		Order order = found.get();
		// an order only moves once. anything after that is a repeat message or a late
		// reply from a step we already gave up on
		if (order.getStatus() != OrderStatus.NEW) {
			log.info("order {} is already {}, ignoring", orderId, order.getStatus());
			return;
		}

		order.setStatus(status);
		log.info("order {} is now {}", orderId, status);

		if (status == OrderStatus.CONFIRMED) {
			publisher.publish(OrderConfirmed.of(order.getId(), order.getCustomerId(),
					order.getItem(), order.getQuantity()));
		}
		else {
			publisher.publish(OrderCancelled.of(order.getId(), order.getCustomerId(),
					order.getItem(), order.getQuantity(), reason));
		}
	}

	@Transactional(readOnly = true)
	public Optional<Order> findById(UUID id) {
		return orders.findById(id);
	}

}
