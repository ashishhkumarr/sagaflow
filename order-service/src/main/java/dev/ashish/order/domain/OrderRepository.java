package dev.ashish.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

	// leans on the (status, updated_at) index
	@Query("select o from Order o where o.status in ('AWAITING_STOCK', 'AWAITING_PAYMENT') "
			+ "and o.updatedAt < :cutoff")
	List<Order> findWaitingSince(@Param("cutoff") Instant cutoff);

	List<Order> findTop50ByOrderByCreatedAtDesc();

}
