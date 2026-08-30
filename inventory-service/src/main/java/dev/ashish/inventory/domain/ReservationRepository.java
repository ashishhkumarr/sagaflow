package dev.ashish.inventory.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

	@Query("select r from Reservation r where r.status = 'RESERVED' and r.createdAt < :cutoff")
	List<Reservation> findHeldSince(@Param("cutoff") Instant cutoff);

}
