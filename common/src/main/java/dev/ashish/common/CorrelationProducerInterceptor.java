package dev.ashish.common;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.nio.charset.StandardCharsets;
import java.util.Map;

// stamps the current id onto outgoing messages. kafka news this up itself so it
// needs a no arg constructor
public class CorrelationProducerInterceptor implements ProducerInterceptor<Object, Object> {

	@Override
	public ProducerRecord<Object, Object> onSend(ProducerRecord<Object, Object> record) {
		String id = Correlation.current();
		if (id != null && record.headers().lastHeader(Correlation.HEADER) == null) {
			record.headers().add(Correlation.HEADER, id.getBytes(StandardCharsets.UTF_8));
		}
		return record;
	}

	@Override
	public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
	}

	@Override
	public void close() {
	}

	@Override
	public void configure(Map<String, ?> configs) {
	}

}
