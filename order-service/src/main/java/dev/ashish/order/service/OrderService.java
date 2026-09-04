package dev.ashish.order.service;

import dev.ashish.contracts.OrderCreated;
import dev.ashish.order.domain.Order;
import dev.ashish.order.domain.OrderRepository;
import dev.ashish.order.messaging.OrderEventPublisher;
import dev.ashish.order.saga.OrderSaga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

	private static final Logger log = LoggerFactory.getLogger(OrderService.class);

	private final OrderRepository orders;

	private final OrderEventPublisher publisher;

	private final OrderSaga saga;

	public OrderService(OrderRepository orders, OrderEventPublisher publisher, OrderSaga saga) {
		this.orders = orders;
		this.publisher = publisher;
		this.saga = saga;
	}

	@Transactional
	public Order placeOrder(String customerId, String item, int quantity, BigDecimal amount) {
		Order order = orders.save(new Order(customerId, item, quantity, amount));
		log.info("saved order {} for customer {}", order.getId(), customerId);

		// just an announcement, the real work starts in the saga below
		publisher.publish(OrderCreated.of(order.getId(), order.getCustomerId(),
				order.getItem(), order.getQuantity(), order.getAmount()));

		saga.start(order);
		return order;
	}

	@Transactional(readOnly = true)
	public Optional<Order> findById(UUID id) {
		return orders.findById(id);
	}

	@Transactional(readOnly = true)
	public List<Order> recent() {
		return orders.findTop50ByOrderByCreatedAtDesc();
	}

}
