package dev.ashish.inventory.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {

	@Id
	private UUID orderId;

	private String item;

	private int quantity;

	@Enumerated(EnumType.STRING)
	private ReservationStatus status;

	private Instant createdAt;

	protected Reservation() {
	}

	public Reservation(UUID orderId, String item, int quantity) {
		this.orderId = orderId;
		this.item = item;
		this.quantity = quantity;
		this.status = ReservationStatus.RESERVED;
		this.createdAt = Instant.now();
	}

	public UUID getOrderId() {
		return orderId;
	}

	public String getItem() {
		return item;
	}

	public int getQuantity() {
		return quantity;
	}

	public ReservationStatus getStatus() {
		return status;
	}

	public void setStatus(ReservationStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
