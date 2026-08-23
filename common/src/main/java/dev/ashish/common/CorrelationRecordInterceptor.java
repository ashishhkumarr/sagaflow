package dev.ashish.common;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.listener.RecordInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

// pulls the id off the kafka header into MDC before the listener runs
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
		// threads get reused, an old id left here would tag the next message wrong
		Correlation.clear();
	}

}
