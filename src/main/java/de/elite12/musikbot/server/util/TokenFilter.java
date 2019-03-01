package de.elite12.musikbot.server.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.services.UserService;

@Component
public class TokenFilter implements Filter{
	
	@Autowired
	private UserService userservice;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if(request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest) request;
			String token = getToken(req.getHeader("Authorization"));
			
			User u = userservice.findUserbyToken(token);
			if(u != null) {
				UserPrincipal up = new UserPrincipal(u);
				SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(up, "", up.getAuthorities()));
			}
			else if(token != null && !token.isEmpty()) {
				throw new BadCredentialsException("Token invalid");
			}
			chain.doFilter(request, response);
		}
	}
	
	private String getToken(String raw) {
		if (raw != null && raw.startsWith("Bearer ")) {
            return raw.substring(7, raw.length());
        }
        return null;
	}
	

}
