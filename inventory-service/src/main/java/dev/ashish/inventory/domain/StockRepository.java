package dev.ashish.inventory.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockRepository extends JpaRepository<Stock, String> {

	// has to be one statement, otherwise two orders read the same count and both think
	// there is enough. returns 0 when there was not
	@Modifying
	@Query("update Stock s set s.available = s.available - :quantity "
			+ "where s.item = :item and s.available >= :quantity")
	int reserveIfAvailable(@Param("item") String item, @Param("quantity") int quantity);

	@Modifying
	@Query("update Stock s set s.available = s.available + :quantity where s.item = :item")
	int giveBack(@Param("item") String item, @Param("quantity") int quantity);

}
