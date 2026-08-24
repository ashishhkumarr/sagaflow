package dev.ashish.order.domain;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum OrderStatus {

	NEW,
	AWAITING_STOCK,
	AWAITING_PAYMENT,
	COMPENSATING,
	CONFIRMED,
	CANCELLED;

	// the whole saga in one place. a move that is not listed is not allowed, which is
	// what turns away repeat and out of order replies.
	// once stock is reserved a failure cannot go straight to cancelled, it has to pass
	// through compensating so the reservation actually gets handed back
	private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
			NEW, EnumSet.of(AWAITING_STOCK),
			AWAITING_STOCK, EnumSet.of(AWAITING_PAYMENT, CANCELLED),
			AWAITING_PAYMENT, EnumSet.of(CONFIRMED, COMPENSATING),
			COMPENSATING, EnumSet.of(CANCELLED),
			CONFIRMED, EnumSet.noneOf(OrderStatus.class),
			CANCELLED, EnumSet.noneOf(OrderStatus.class));

	public boolean canMoveTo(OrderStatus next) {
		return ALLOWED.get(this).contains(next);
	}

	public boolean isFinished() {
		return this == CONFIRMED || this == CANCELLED;
	}

}
