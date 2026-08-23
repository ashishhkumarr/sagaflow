package dev.ashish.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.RecordInterceptor;

@Configuration
public class CorrelationConfig {

	// boot finds a RecordInterceptor bean by itself, no other wiring needed
	@Bean
	RecordInterceptor<Object, Object> correlationRecordInterceptor() {
		return new CorrelationRecordInterceptor();
	}

}
