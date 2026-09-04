package dev.ashish.order.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// the dashboard is served from a different port in development, so the browser needs
// telling that calls from there are fine
@Configuration
public class CorsConfig implements WebMvcConfigurer {

	private final String allowedOrigin;

	public CorsConfig(@Value("${dashboard.origin}") String allowedOrigin) {
		this.allowedOrigin = allowedOrigin;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/orders/**")
				.allowedOrigins(allowedOrigin)
				.allowedMethods("GET", "POST");
		registry.addMapping("/orders")
				.allowedOrigins(allowedOrigin)
				.allowedMethods("GET", "POST");
	}

}
