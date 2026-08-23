package dev.ashish.payment.messaging;

import dev.ashish.contracts.PaymentCommand;
import dev.ashish.contracts.ProcessPayment;
import dev.ashish.contracts.Topics;
import dev.ashish.payment.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProcessPaymentListener {

	private final PaymentService payments;

	public ProcessPaymentListener(PaymentService payments) {
		this.payments = payments;
	}

	@KafkaListener(topics = Topics.PAYMENT_COMMANDS)
	public void onCommand(PaymentCommand command) {
		switch (command) {
			case ProcessPayment pay -> payments.pay(pay);
		}
	}

}
