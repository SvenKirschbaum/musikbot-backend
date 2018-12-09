package de.elite12.musikbot.server.api;

import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

@Provider
public class ExceptionListener implements ApplicationEventListener {

    @Override
    public void onEvent(ApplicationEvent event) {

    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return new ExceptionRequestEventListener();
    }

    public static class ExceptionRequestEventListener implements RequestEventListener{
        private final Logger logger;

        public ExceptionRequestEventListener(){
            logger = Logger.getLogger(getClass());
        }

        @Override
        public void onEvent(RequestEvent event) {
            switch (event.getType()){
                case ON_EXCEPTION:
                    Throwable t = event.getException();
                    logger.error("Found exception: "+t.getClass().getCanonicalName(), t);
                    break;
                default:
                	break;
            }
        }
    }
}
