package dev.ashish.payment.service;

import dev.ashish.contracts.PaymentFailed;
import dev.ashish.contracts.PaymentSucceeded;
import dev.ashish.contracts.ProcessPayment;
import dev.ashish.payment.domain.Payment;
import dev.ashish.payment.domain.PaymentRepository;
import dev.ashish.payment.messaging.PaymentEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

	private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

	private final PaymentRepository payments;

	private final FakeCardGateway gateway;

	private final PaymentEventPublisher publisher;

	public PaymentService(PaymentRepository payments, FakeCardGateway gateway, PaymentEventPublisher publisher) {
		this.payments = payments;
		this.gateway = gateway;
		this.publisher = publisher;
	}

	@Transactional
	public void pay(ProcessPayment command) {
		// order id is the primary key on payments, so if a row is already there this
		// message has been through here before and the card must not be charged again
		if (payments.existsById(command.orderId())) {
			log.info("order {} already paid for, skipping", command.orderId());
			return;
		}

		FakeCardGateway.ChargeResult result = gateway.charge(command.customerId(), command.amount());

		if (!result.approved()) {
			payments.save(Payment.declined(command.orderId(), command.customerId(), command.amount(), result.reason()));
			log.info("declined order {}: {}", command.orderId(), result.reason());
			publisher.publish(PaymentFailed.of(command.orderId(), command.customerId(),
					command.amount(), result.reason()));
			return;
		}

		payments.save(Payment.charged(command.orderId(), command.customerId(), command.amount()));
		log.info("charged {} to {} for order {}", command.amount(), command.customerId(), command.orderId());
		publisher.publish(PaymentSucceeded.of(command.orderId(), command.customerId(), command.amount()));
	}

}
