package dev.ashish.contracts;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

// every event carries a "type" field in the json so one topic can hold more than one
// kind of event and the consumer still knows what it is reading
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = OrderCreated.class, name = "order-created"),
		@JsonSubTypes.Type(value = OrderConfirmed.class, name = "order-confirmed"),
		@JsonSubTypes.Type(value = OrderCancelled.class, name = "order-cancelled")
})
public sealed interface OrderEvent permits OrderCreated, OrderConfirmed, OrderCancelled {

	UUID orderId();

}
