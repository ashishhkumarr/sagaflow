package dev.ashish.order.service;

import dev.ashish.order.domain.Order;
import dev.ashish.order.domain.OrderRepository;
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

	public OrderService(OrderRepository orders) {
		this.orders = orders;
	}

	@Transactional
	public Order placeOrder(String customerId, String item, int quantity, BigDecimal amount) {
		Order order = orders.save(new Order(customerId, item, quantity, amount));
		log.info("saved order {} for customer {}", order.getId(), customerId);
		return order;
	}

	@Transactional(readOnly = true)
	public Optional<Order> findById(UUID id) {
		return orders.findById(id);
	}

}
