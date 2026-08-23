package dev.ashish.contracts;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

// a command is an instruction aimed at one service and it can be refused. an event
// is something that already happened. keeping them on separate topics keeps that clear
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = ReserveStock.class, name = "reserve-stock")
})
public sealed interface InventoryCommand permits ReserveStock {

	UUID orderId();

}
