package dev.ashish.outbox;

import dev.ashish.common.Correlation;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

// nothing goes straight to kafka any more. the row lands in the same transaction as
// whatever changed the order, so either both stick or neither does
@Component
public class Outbox {

	private final OutboxRepository outbox;

	private final JsonMapper json;

	public Outbox(OutboxRepository outbox, JsonMapper json) {
		this.outbox = outbox;
		this.json = json;
	}

	public void put(String topic, String key, Object message) {
		outbox.save(new OutboxMessage(topic, key, json.writeValueAsString(message), Correlation.current()));
	}

}
