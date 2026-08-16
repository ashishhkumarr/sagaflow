package dev.ashish.order.web;

import dev.ashish.order.domain.Order;
import dev.ashish.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
		Order order = orderService.placeOrder(
				request.customerId(),
				request.item(),
				request.quantity(),
				request.amount());

		return ResponseEntity
				.created(URI.create("/orders/" + order.getId()))
				.body(OrderResponse.from(order));
	}

	@GetMapping("/{id}")
	public OrderResponse getOne(@PathVariable UUID id) {
		return orderService.findById(id)
				.map(OrderResponse::from)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no order with id " + id));
	}

}
