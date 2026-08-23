package dev.ashish.contracts;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = ProcessPayment.class, name = "process-payment")
})
public sealed interface PaymentCommand permits ProcessPayment {

	UUID orderId();

}
