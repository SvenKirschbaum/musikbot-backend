package de.elite12.musikbot.backend.aspects;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class MessageHandlerObservationAspect {
    @Autowired
    ObservationRegistry observationRegistry;

    @Around("@annotation(org.springframework.messaging.handler.annotation.MessageMapping)")
    public Object traceMessageMappingHandler(ProceedingJoinPoint joinPoint) throws Throwable {
        return this.observeHandler("MessageMappingHandler", joinPoint);
    }

    @Around("@annotation(org.springframework.messaging.simp.annotation.SubscribeMapping)")
    public Object traceSubscribeMappingHandler(ProceedingJoinPoint joinPoint) throws Throwable {
        return this.observeHandler("SubscribeMappingHandler", joinPoint);
    }

    private Object observeHandler(String prefix, ProceedingJoinPoint joinPoint) throws Throwable {
        Observation observation = Observation.createNotStarted("%s %s".formatted(prefix, joinPoint.getSignature().getName()), observationRegistry);

        observation.lowCardinalityKeyValue("class", joinPoint.getTarget().getClass().getSimpleName());
        observation.lowCardinalityKeyValue("method", joinPoint.getSignature().getName());

        try (Observation.Scope scope = observation.start().openScope()) {
            return joinPoint.proceed();
        } catch (Throwable e) {
            observation.error(e);
            throw e;
        } finally {
            observation.stop();
        }
    }
}
