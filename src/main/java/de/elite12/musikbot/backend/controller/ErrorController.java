package de.elite12.musikbot.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController implements org.springframework.boot.webmvc.error.ErrorController {

	private final Logger logger = LoggerFactory.getLogger(ErrorController.class);

	@RequestMapping("/error")
    public void handleError(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
		Integer statusCode = (Integer) request.getAttribute(jakarta.servlet.RequestDispatcher.ERROR_STATUS_CODE);
		Exception exception = (Exception) request.getAttribute(jakarta.servlet.RequestDispatcher.ERROR_EXCEPTION);
		String errormsg = (String) request.getAttribute(jakarta.servlet.RequestDispatcher.ERROR_MESSAGE);
		String path = (String) request.getAttribute(jakarta.servlet.RequestDispatcher.FORWARD_REQUEST_URI);

		Object p = SecurityContextHolder.getContext().getAuthentication() != null ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;

		logger.warn(String.format("Error for User: %s, Error Code: %d Message: %s Path: %s", p, statusCode, errormsg, path));

		if (exception != null) {
			throw exception;
		}
	}
}
