package dev.ashish.payment.messaging;

import dev.ashish.contracts.InventoryEvent;
import dev.ashish.contracts.StockReserved;
import dev.ashish.contracts.Topics;
import dev.ashish.payment.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StockReservedListener {

	private final PaymentService payments;

	public StockReservedListener(PaymentService payments) {
		this.payments = payments;
	}

	@KafkaListener(topics = Topics.INVENTORY_EVENTS)
	public void onInventoryEvent(InventoryEvent event) {
		// nothing to charge if the stock was never reserved
		if (event instanceof StockReserved reserved) {
			payments.pay(reserved);
		}
	}

}
