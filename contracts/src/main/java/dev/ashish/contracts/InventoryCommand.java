package dev.ashish.contracts;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

// commands can be refused, events already happened. separate topics so that stays clear
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = ReserveStock.class, name = "reserve-stock")
})
public sealed interface InventoryCommand permits ReserveStock {

	UUID orderId();

}
