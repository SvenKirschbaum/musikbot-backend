package de.elite12.musikbot.server.filter;

import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class TokenFilter implements Filter{

	@Autowired
	private UserService userservice;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if(request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest) request;
			String authheader = req.getHeader("Authorization");
			String token = TokenFilter.parseHeader(authheader);
			
			User u = userservice.findUserbyToken(token);
			if(u != null) {
				UserPrincipal up = new UserPrincipal(u);
				SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(up, "", up.getAuthorities()));
			}
			else if(authheader != null && !authheader.isEmpty()) {
				HttpServletResponse httpServletResponse = (HttpServletResponse) response;
				httpServletResponse.sendError(401,"Authorization Header invalid");
				return;
			}
			chain.doFilter(request, response);
		}
	}

	/**
	 * Parses an Authorization Header and returns the token contained in it
	 * @param raw The Header string
	 * @return the Token or null if string is invalid
	 */
	public static String parseHeader(String raw) {
		if (raw != null && raw.startsWith("Bearer ")) {
            return raw.substring(7);
        }
        return null;
	}
	

}
