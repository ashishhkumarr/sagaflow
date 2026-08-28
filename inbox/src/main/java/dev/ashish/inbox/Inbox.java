package dev.ashish.inbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class Inbox {

	private final ProcessedMessageRepository processed;

	private final String consumer;

	public Inbox(ProcessedMessageRepository processed, @Value("${spring.application.name}") String consumer) {
		this.processed = processed;
		this.consumer = consumer;
	}

	public boolean alreadyHandled(UUID messageId) {
		return processed.existsById(messageId);
	}

	// called from inside the same transaction as the real work, so the record of having
	// handled the message and the effect of handling it both land or neither does
	public void markHandled(UUID messageId) {
		processed.save(new ProcessedMessage(messageId, consumer));
	}

}
