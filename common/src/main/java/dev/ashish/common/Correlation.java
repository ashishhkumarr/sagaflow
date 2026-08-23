package dev.ashish.common;

import org.slf4j.MDC;

import java.util.UUID;

// one id that follows an order across all four services so the logs line up
public final class Correlation {

	public static final String HEADER = "correlation-id";

	public static final String MDC_KEY = "correlationId";

	private Correlation() {
	}

	public static String newId() {
		return UUID.randomUUID().toString();
	}

	public static void put(String id) {
		MDC.put(MDC_KEY, id);
	}

	public static void clear() {
		MDC.remove(MDC_KEY);
	}

	public static String current() {
		return MDC.get(MDC_KEY);
	}

}
