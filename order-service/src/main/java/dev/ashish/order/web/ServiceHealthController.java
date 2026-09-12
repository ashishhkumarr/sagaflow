package dev.ashish.order.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

// the browser cannot reach the other three services, so the checking happens here and
// the dashboard just reads the answer. handy for watching one of them go away
@RestController
public class ServiceHealthController {

	private static final Logger log = LoggerFactory.getLogger(ServiceHealthController.class);

	private final Map<String, String> services;

	private final RestClient http = RestClient.create();

	public ServiceHealthController(@Value("${services.inventory}") String inventory,
			@Value("${services.payment}") String payment,
			@Value("${services.notification}") String notification) {
		this.services = Map.of("inventory", inventory, "payment", payment, "notification", notification);
	}

	@GetMapping("/services")
	public List<ServiceState> states() {
		return services.entrySet().stream()
				.map(entry -> new ServiceState(entry.getKey(), isUp(entry.getValue())))
				.sorted((a, b) -> a.name().compareTo(b.name()))
				.toList();
	}

	private boolean isUp(String baseUrl) {
		try {
			String body = http.get().uri(baseUrl + "/actuator/health").retrieve().body(String.class);
			return body != null && body.contains("UP");
		}
		catch (Exception e) {
			log.debug("{} is not answering: {}", baseUrl, e.getMessage());
			return false;
		}
	}

	public record ServiceState(String name, boolean up) {
	}

}
