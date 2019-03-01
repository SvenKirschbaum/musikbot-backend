package de.elite12.musikbot.server.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

@Component
public class CookieFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if(response instanceof HttpServletResponse) {
			HttpServletResponse resp = (HttpServletResponse) response;
			resp.setHeader("Set-Cookie", null);
		}
		chain.doFilter(request, response);
	}

}
