package dev.ashish.order.web;

import dev.ashish.order.domain.OrderStatus;
import dev.ashish.order.domain.OrderStep;

import java.time.Instant;

public record StepResponse(

		OrderStatus status,
		String detail,
		Instant at

) {

	static StepResponse from(OrderStep step) {
		return new StepResponse(step.getStatus(), step.getDetail(), step.getAt());
	}

}
