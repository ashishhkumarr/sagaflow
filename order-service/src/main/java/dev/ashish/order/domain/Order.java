package dev.ashish.order.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

	@Id
	private UUID id;

	private String customerId;

	private String item;

	private int quantity;

	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	private String cancelReason;

	private Instant createdAt;

	private Instant updatedAt;

	// jpa needs this
	protected Order() {
	}

	public Order(String customerId, String item, int quantity, BigDecimal amount) {
		this.id = UUID.randomUUID();
		this.customerId = customerId;
		this.item = item;
		this.quantity = quantity;
		this.amount = amount;
		this.status = OrderStatus.NEW;
		this.createdAt = Instant.now();
		this.updatedAt = this.createdAt;
	}

	// the only way the status is allowed to change. everything that wants to move an
	// order goes through here so the rules cannot be worked around by accident
	public void moveTo(OrderStatus next) {
		if (!status.canMoveTo(next)) {
			throw new InvalidTransition(id, status, next);
		}
		this.status = next;
		this.updatedAt = Instant.now();
	}

	public void cancel(String reason) {
		moveTo(OrderStatus.CANCELLED);
		this.cancelReason = reason;
	}

	public UUID getId() {
		return id;
	}

	public String getCustomerId() {
		return customerId;
	}

	public String getItem() {
		return item;
	}

	public int getQuantity() {
		return quantity;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public String getCancelReason() {
		return cancelReason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

}
