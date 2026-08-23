package dev.ashish.order.messaging;

import dev.ashish.contracts.PaymentEvent;
import dev.ashish.contracts.PaymentFailed;
import dev.ashish.contracts.PaymentSucceeded;
import dev.ashish.contracts.Topics;
import dev.ashish.order.saga.OrderSaga;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

	private final OrderSaga saga;

	public PaymentEventListener(OrderSaga saga) {
		this.saga = saga;
	}

	@KafkaListener(topics = Topics.PAYMENT_EVENTS,
			properties = "spring.json.value.default.type=dev.ashish.contracts.PaymentEvent")
	public void onPaymentEvent(PaymentEvent event) {
		switch (event) {
			case PaymentSucceeded succeeded -> saga.onPaymentSucceeded(succeeded.orderId());
			case PaymentFailed failed -> saga.onPaymentFailed(failed.orderId(), failed.reason());
		}
	}

}
