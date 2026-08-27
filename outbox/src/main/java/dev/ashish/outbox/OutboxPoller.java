package dev.ashish.outbox;

import dev.ashish.common.Correlation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPoller {

	private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

	private static final int BATCH = 50;

	private final OutboxRepository outbox;

	private final KafkaTemplate<String, Object> kafka;

	private final JsonMapper json;

	public OutboxPoller(OutboxRepository outbox, KafkaTemplate<String, Object> kafka, JsonMapper json) {
		this.outbox = outbox;
		this.kafka = kafka;
		this.json = json;
	}

	@Scheduled(fixedDelayString = "${outbox.poll-ms}")
	@Transactional
	public void flush() {
		List<OutboxMessage> batch = outbox.claimUnsent(BATCH);

		for (OutboxMessage message : batch) {
			Correlation.put(message.getCorrelationId());
			try {
				// blocks on the ack on purpose. marking a row sent before the broker has
				// it would lose the message for good
				kafka.send(message.getTopic(), message.getMessageKey(), json.readTree(message.getPayload()))
						.get(10, TimeUnit.SECONDS);
				message.markSent();
			}
			catch (Exception e) {
				// almost always the broker being unreachable, so the rest of the batch
				// will fail too. leave them unsent and try again next time round
				log.warn("could not send outbox row {}: {}", message.getId(), e.getMessage());
				break;
			}
			finally {
				Correlation.clear();
			}
		}
	}

}
