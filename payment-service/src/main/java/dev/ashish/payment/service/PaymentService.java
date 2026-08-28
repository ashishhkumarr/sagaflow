package dev.ashish.payment.service;

import dev.ashish.contracts.PaymentFailed;
import dev.ashish.contracts.PaymentSucceeded;
import dev.ashish.contracts.ProcessPayment;
import dev.ashish.inbox.Inbox;
import dev.ashish.payment.domain.Payment;
import dev.ashish.payment.domain.PaymentRepository;
import dev.ashish.payment.domain.PaymentStatus;
import dev.ashish.payment.messaging.PaymentEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentService {

	private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

	private final PaymentRepository payments;

	private final FakeCardGateway gateway;

	private final PaymentEventPublisher publisher;

	private final Inbox inbox;

	public PaymentService(PaymentRepository payments, FakeCardGateway gateway,
			PaymentEventPublisher publisher, Inbox inbox) {
		this.payments = payments;
		this.gateway = gateway;
		this.publisher = publisher;
		this.inbox = inbox;
	}

	@Transactional
	public void pay(ProcessPayment command) {
		if (inbox.alreadyHandled(command.commandId())) {
			log.info("already handled this command for order {}, replying again", command.orderId());
			replyAgain(command);
			return;
		}
		inbox.markHandled(command.commandId());

		// a different command for an order that was already paid. the inbox cannot catch
		// that one, the primary key on order id is what stops a second charge
		if (payments.existsById(command.orderId())) {
			log.info("order {} was already paid for, replying again", command.orderId());
			replyAgain(command);
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

	// the payment row already holds what was decided, so the repeat answer is the exact
	// same one rather than a guess
	private void replyAgain(ProcessPayment command) {
		Optional<Payment> existing = payments.findById(command.orderId());
		if (existing.isEmpty()) {
			return;
		}

		Payment payment = existing.get();
		if (payment.getStatus() == PaymentStatus.CHARGED) {
			publisher.publish(PaymentSucceeded.of(payment.getOrderId(), payment.getCustomerId(),
					payment.getAmount()));
		}
		else {
			publisher.publish(PaymentFailed.of(payment.getOrderId(), payment.getCustomerId(),
					payment.getAmount(), payment.getReason()));
		}
	}

}
