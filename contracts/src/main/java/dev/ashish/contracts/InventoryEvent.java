package dev.ashish.contracts;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = StockReserved.class, name = "stock-reserved"),
		@JsonSubTypes.Type(value = StockRejected.class, name = "stock-rejected")
})
public sealed interface InventoryEvent permits StockReserved, StockRejected {

	UUID orderId();

}
