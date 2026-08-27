package dev.ashish.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox")
public class OutboxMessage {

	@Id
	private UUID id;

	private String topic;

	private String messageKey;

	@Column(columnDefinition = "text")
	private String payload;

	private String correlationId;

	private Instant createdAt;

	private Instant sentAt;

	protected OutboxMessage() {
	}

	public OutboxMessage(String topic, String messageKey, String payload, String correlationId) {
		this.id = UUID.randomUUID();
		this.topic = topic;
		this.messageKey = messageKey;
		this.payload = payload;
		this.correlationId = correlationId;
		this.createdAt = Instant.now();
	}

	public void markSent() {
		this.sentAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public String getTopic() {
		return topic;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public String getPayload() {
		return payload;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public Instant getSentAt() {
		return sentAt;
	}

}
