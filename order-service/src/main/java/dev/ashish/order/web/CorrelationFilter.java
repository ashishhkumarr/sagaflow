package dev.ashish.order.web;

import dev.ashish.common.Correlation;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

// id starts here since this is the only way in from outside. a caller supplied one
// is kept, otherwise a new one gets made
@Component
public class CorrelationFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String fromCaller = ((HttpServletRequest) request).getHeader(Correlation.HEADER);
		String id = (fromCaller != null && !fromCaller.isBlank()) ? fromCaller : Correlation.newId();

		Correlation.put(id);
		// hand it back so whoever called can quote it when something looks wrong
		((HttpServletResponse) response).setHeader(Correlation.HEADER, id);

		try {
			chain.doFilter(request, response);
		}
		finally {
			Correlation.clear();
		}
	}

}
