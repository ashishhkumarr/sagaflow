package dev.ashish.common;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.listener.RecordInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

// runs before every consumed record, pulls the id off the kafka header and puts it
// in MDC so anything the listener logs is tagged with it
public class CorrelationRecordInterceptor implements RecordInterceptor<Object, Object> {

	@Override
	public ConsumerRecord<Object, Object> intercept(ConsumerRecord<Object, Object> record,
			Consumer<Object, Object> consumer) {

		Header header = record.headers().lastHeader(Correlation.HEADER);
		String id = header != null
				? new String(header.value(), StandardCharsets.UTF_8)
				: UUID.randomUUID().toString();

		Correlation.put(id);
		return record;
	}

	@Override
	public void afterRecord(ConsumerRecord<Object, Object> record, Consumer<Object, Object> consumer) {
		// consumer threads get reused, leaving the old id behind would tag the next
		// message with the wrong order
		Correlation.clear();
	}

}
