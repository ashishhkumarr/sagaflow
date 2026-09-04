package dev.ashish.order.web;

import dev.ashish.order.domain.Order;
import dev.ashish.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(

		UUID id,
		String customerId,
		String item,
		int quantity,
		BigDecimal amount,
		OrderStatus status,
		String cancelReason,
		Instant createdAt,
		Instant updatedAt

) {

	static OrderResponse from(Order order) {
		return new OrderResponse(
				order.getId(),
				order.getCustomerId(),
				order.getItem(),
				order.getQuantity(),
				order.getAmount(),
				order.getStatus(),
				order.getCancelReason(),
				order.getCreatedAt(),
				order.getUpdatedAt());
	}

}
