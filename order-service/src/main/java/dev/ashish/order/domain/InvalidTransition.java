package dev.ashish.order.domain;

import java.util.UUID;

public class InvalidTransition extends RuntimeException {

	public InvalidTransition(UUID orderId, OrderStatus from, OrderStatus to) {
		super("order " + orderId + " cannot go from " + from + " to " + to);
	}

}
