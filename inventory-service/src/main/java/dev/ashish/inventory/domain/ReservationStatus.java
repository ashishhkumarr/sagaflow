package dev.ashish.inventory.domain;

public enum ReservationStatus {

	RESERVED,
	RELEASED,

	// the order went through, this stock is sold and must not be swept back
	COMMITTED

}
