package dev.ashish.order.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

// one row per move the order made. the orders table only knows where it is now, this is
// how it got there, which is what the dashboard draws
@Entity
@Table(name = "order_steps")
public class OrderStep {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private UUID orderId;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	private String detail;

	private Instant at;

	protected OrderStep() {
	}

	public OrderStep(UUID orderId, OrderStatus status, String detail) {
		this.orderId = orderId;
		this.status = status;
		this.detail = detail;
		this.at = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public UUID getOrderId() {
		return orderId;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public String getDetail() {
		return detail;
	}

	public Instant getAt() {
		return at;
	}

}
