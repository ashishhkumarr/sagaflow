package dev.ashish.order.saga;

import dev.ashish.order.domain.Order;
import dev.ashish.order.domain.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class StuckOrderSweep {

	private static final Logger log = LoggerFactory.getLogger(StuckOrderSweep.class);

	private final OrderRepository orders;

	private final OrderSaga saga;

	private final Duration waitBefore;

	public StuckOrderSweep(OrderRepository orders, OrderSaga saga,
			@Value("${saga.retry-after}") Duration waitBefore) {
		this.orders = orders;
		this.saga = saga;
		this.waitBefore = waitBefore;
	}

	@Scheduled(fixedDelayString = "${saga.sweep-ms}")
	@Transactional
	public void nudgeStuckOrders() {
		List<Order> waiting = orders.findWaitingSince(Instant.now().minus(waitBefore));

		for (Order order : waiting) {
			log.warn("order {} has been {} for over {}, sending the command again",
					order.getId(), order.getStatus(), waitBefore);
			saga.resend(order);
		}
	}

}
