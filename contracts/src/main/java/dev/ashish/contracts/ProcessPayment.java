package dev.ashish.contracts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProcessPayment(

		UUID commandId,
		UUID orderId,
		String customerId,
		BigDecimal amount,
		Instant sentAt

) implements PaymentCommand {

	public static ProcessPayment of(UUID orderId, String customerId, BigDecimal amount) {
		return new ProcessPayment(UUID.randomUUID(), orderId, customerId, amount, Instant.now());
	}

}
