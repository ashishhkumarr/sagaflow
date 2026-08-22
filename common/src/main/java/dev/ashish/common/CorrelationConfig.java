package dev.ashish.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.RecordInterceptor;

@Configuration
public class CorrelationConfig {

	// spring boot picks a RecordInterceptor bean up on its own and hands it to the
	// listener container factory, so this is all the wiring the consumer side needs
	@Bean
	RecordInterceptor<Object, Object> correlationRecordInterceptor() {
		return new CorrelationRecordInterceptor();
	}

}
