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

	private Instant createdAt;

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

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
