package dev.ashish.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {

	// skip locked means a second instance of this service picks up different rows
	// instead of blocking on the ones this one already has. has to be a native query,
	// jpql has no way to say it
	@Query(value = """
			select * from outbox
			where sent_at is null
			order by created_at
			for update skip locked
			limit :limit
			""", nativeQuery = true)
	List<OutboxMessage> claimUnsent(@Param("limit") int limit);

	long countBySentAtIsNull();

}
