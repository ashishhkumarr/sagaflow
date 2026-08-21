package dev.ashish.order.messaging;

import dev.ashish.contracts.PaymentEvent;
import dev.ashish.contracts.PaymentFailed;
import dev.ashish.contracts.PaymentSucceeded;
import dev.ashish.contracts.Topics;
import dev.ashish.order.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

	private final OrderService orderService;

	public PaymentEventListener(OrderService orderService) {
		this.orderService = orderService;
	}

	@KafkaListener(topics = Topics.PAYMENT_EVENTS,
			properties = "spring.json.value.default.type=dev.ashish.contracts.PaymentEvent")
	public void onPaymentEvent(PaymentEvent event) {
		if (event instanceof PaymentSucceeded succeeded) {
			orderService.confirm(succeeded.orderId());
		}
		else if (event instanceof PaymentFailed failed) {
			orderService.cancel(failed.orderId(), failed.reason());
		}
	}

}
