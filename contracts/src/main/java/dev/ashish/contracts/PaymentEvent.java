package dev.ashish.contracts;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = PaymentSucceeded.class, name = "payment-succeeded"),
		@JsonSubTypes.Type(value = PaymentFailed.class, name = "payment-failed")
})
public sealed interface PaymentEvent permits PaymentSucceeded, PaymentFailed {

	UUID eventId();

	UUID orderId();

}
