package dev.ashish.payment.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

	@Id
	private UUID orderId;

	private String customerId;

	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status;

	private String reason;

	private Instant createdAt;

	protected Payment() {
	}

	private Payment(UUID orderId, String customerId, BigDecimal amount, PaymentStatus status, String reason) {
		this.orderId = orderId;
		this.customerId = customerId;
		this.amount = amount;
		this.status = status;
		this.reason = reason;
		this.createdAt = Instant.now();
	}

	public static Payment charged(UUID orderId, String customerId, BigDecimal amount) {
		return new Payment(orderId, customerId, amount, PaymentStatus.CHARGED, null);
	}

	public static Payment declined(UUID orderId, String customerId, BigDecimal amount, String reason) {
		return new Payment(orderId, customerId, amount, PaymentStatus.DECLINED, reason);
	}

	public UUID getOrderId() {
		return orderId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public String getReason() {
		return reason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
