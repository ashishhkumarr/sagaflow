package dev.ashish.inbox;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_messages")
public class ProcessedMessage {

	// message ids are uuids so they are unique across every topic this service reads,
	// no need for a composite key with the consumer name
	@Id
	private UUID messageId;

	private String consumer;

	private Instant handledAt;

	protected ProcessedMessage() {
	}

	public ProcessedMessage(UUID messageId, String consumer) {
		this.messageId = messageId;
		this.consumer = consumer;
		this.handledAt = Instant.now();
	}

	public UUID getMessageId() {
		return messageId;
	}

	public String getConsumer() {
		return consumer;
	}

	public Instant getHandledAt() {
		return handledAt;
	}

}
