package de.elite12.musikbot.server.services;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import de.elite12.musikbot.server.data.UserMessage;

@Service
@Configuration
public class MessageService implements WebMvcConfigurer {
	
	private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
	
	private @Autowired HttpSession session;
	
	@Autowired 
	MessageServiceInterceptor messageServiceInterceptor;

	
	public void addMessage(String message, String type) {
        logger.debug("Adding Message to User Session");
        @SuppressWarnings("unchecked")
        List<UserMessage> msgs = (List<UserMessage>) session.getAttribute("msg");
        if (msgs == null) {
            msgs = new LinkedList<UserMessage>();
        }
        msgs.add(new UserMessage(message, type));
        session.setAttribute("msg", msgs);
    }
	
	@Override
	  public void addInterceptors(InterceptorRegistry registry) {
	    registry.addInterceptor(messageServiceInterceptor);
	  }
	
	
	
	@Component
	public static class MessageServiceInterceptor implements HandlerInterceptor {
		
		private @Autowired HttpSession session;
		
		@Override
		public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
			if(handler instanceof HandlerMethod) {
				HandlerMethod hm = (HandlerMethod) handler;
				if(hm.getBeanType().getPackage().getName().startsWith("de.elite12.musikbot.server.controller")) {
					@SuppressWarnings("unchecked")
					List<UserMessage> msgs = (List<UserMessage>) session.getAttribute("msg");
					if(msgs != null) {
						ArrayList<UserMessage> copy = new ArrayList<>(msgs);
						msgs.clear();
						session.setAttribute("msg", msgs);
						modelAndView.addObject("messages", copy);
						return;
					}
					modelAndView.addObject("messages", new ArrayList<UserMessage>());
					return;
				}
			}
		}
	}
}
